package com.intellij.psi.impl.source;

import com.intellij.lang.Language;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LanguageLevelProjectExtension;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.PsiImplUtil;
import com.intellij.psi.impl.java.stubs.JavaStubElementTypes;
import com.intellij.psi.impl.java.stubs.PsiJavaFileStub;
import com.intellij.psi.impl.source.resolve.ClassResolverProcessor;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.impl.source.tree.ChildRole;
import com.intellij.psi.scope.ElementClassHint;
import com.intellij.psi.scope.JavaScopeProcessorEvent;
import com.intellij.psi.scope.NameHint;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.ParameterizedCachedValue;
import com.intellij.psi.util.ParameterizedCachedValueProvider;
import com.intellij.psi.util.PsiUtil;
import com.intellij.reference.SoftReference;
import com.intellij.util.ConcurrencyUtil;
import com.intellij.util.containers.ConcurrentHashMap;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public abstract class PsiJavaFileBaseImpl extends PsiFileImpl implements PsiJavaFile {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.source.PsiJavaFileBaseImpl");
  private static final Key<ResolveCache.MapPair<PsiJavaFile,ConcurrentMap<String, SoftReference<JavaResolveResult[]>>>> CACHED_CLASSES_MAP_KEY = Key.create("PsiJavaFileBaseImpl.CACHED_CLASSES_MAP_KEY");

  private final ConcurrentMap<PsiJavaFile,ConcurrentMap<String, SoftReference<JavaResolveResult[]>>> myGuessCache;

  @NonNls private static final String[] IMPLICIT_IMPORTS = new String[]{ "java.lang" };
  private final ParameterizedCachedValueProvider<LanguageLevel,PsiJavaFile> myLanguageLevelProvider = new ParameterizedCachedValueProvider<LanguageLevel, PsiJavaFile>() {
    public CachedValueProvider.Result<LanguageLevel> compute(PsiJavaFile param) {
      LanguageLevel level = getLanguageLevelInner();
      return CachedValueProvider.Result.create(level, ProjectRootManager.getInstance(getProject()));
    }
  };

  protected PsiJavaFileBaseImpl(IElementType elementType, IElementType contentElementType, FileViewProvider viewProvider) {
    super(elementType, contentElementType, viewProvider);
    myGuessCache = myManager.getResolveCache().getOrCreateWeakMap(CACHED_CLASSES_MAP_KEY, false);
  }

  @SuppressWarnings({"CloneDoesntDeclareCloneNotSupportedException"})
  protected PsiJavaFileBaseImpl clone() {
    PsiJavaFileBaseImpl clone = (PsiJavaFileBaseImpl)super.clone();
    clone.clearCaches();
    return clone;
  }

  @NotNull
  public PsiClass[] getClasses() {
    final PsiJavaFileStub stub = (PsiJavaFileStub)getStub();
    if (stub != null) {
      return stub.getChildrenByType(JavaStubElementTypes.CLASS, PsiClass.ARRAY_FACTORY);
    }

    return calcTreeElement().getChildrenAsPsiElements(Constants.CLASS_BIT_SET, Constants.PSI_CLASS_ARRAY_CONSTRUCTOR);
  }

  public PsiPackageStatement getPackageStatement() {
    return (PsiPackageStatement)calcTreeElement().findChildByRoleAsPsiElement(ChildRole.PACKAGE_STATEMENT);
  }

  @NotNull
  public String getPackageName() {
    PsiJavaFileStub stub = (PsiJavaFileStub)getStub();
    if (stub != null) {
      return stub.getPackageName();
    }

    PsiPackageStatement statement = getPackageStatement();
    if (statement == null) {
      return "";
    }
    else {
      return statement.getPackageName();
    }
  }

  @NotNull
  public PsiImportList getImportList() {
    final PsiJavaFileStub stub = (PsiJavaFileStub)getStub();
    if (stub != null) {
      return stub.getChildrenByType(JavaStubElementTypes.IMPORT_LIST, PsiImportList.ARRAY_FACTORY)[0];
    }

    return (PsiImportList)calcTreeElement().findChildByRoleAsPsiElement(ChildRole.IMPORT_LIST);
  }

  @NotNull
  public PsiElement[] getOnDemandImports(boolean includeImplicit, boolean checkIncludes) {
    List<PsiElement> array = new ArrayList<PsiElement>();

    PsiImportList importList = getImportList();
    PsiImportStatement[] statements = importList.getImportStatements();
    for (PsiImportStatement statement : statements) {
      if (statement.isOnDemand()) {
        PsiElement resolved = statement.resolve();
        if (resolved != null) {
          array.add(resolved);
        }
      }
    }

    if (includeImplicit){
      PsiJavaCodeReferenceElement[] implicitRefs = getImplicitlyImportedPackageReferences();
      for (PsiJavaCodeReferenceElement implicitRef : implicitRefs) {
        final PsiElement resolved = implicitRef.resolve();
        if (resolved != null) {
          array.add(resolved);
        }
      }
    }

    return array.toArray(new PsiElement[array.size()]);
  }

  @NotNull
  public PsiClass[] getSingleClassImports(boolean checkIncludes) {
    List<PsiClass> array = new ArrayList<PsiClass>();
    PsiImportList importList = getImportList();
    PsiImportStatement[] statements = importList.getImportStatements();
    for (PsiImportStatement statement : statements) {
      if (!statement.isOnDemand()) {
        PsiElement ref = statement.resolve();
        if (ref instanceof PsiClass) {
          array.add((PsiClass)ref);
        }
      }
    }
    return array.toArray(new PsiClass[array.size()]);
  }

  public PsiJavaCodeReferenceElement findImportReferenceTo(PsiClass aClass) {
    PsiImportList importList = getImportList();
    PsiImportStatement[] statements = importList.getImportStatements();
    for (PsiImportStatement statement : statements) {
      if (!statement.isOnDemand()) {
        PsiElement ref = statement.resolve();
        if (ref != null && getManager().areElementsEquivalent(ref, aClass)) {
          return statement.getImportReference();
        }
      }
    }
    return null;
  }

  @NotNull
  public String[] getImplicitlyImportedPackages() {
    return IMPLICIT_IMPORTS;
  }

  @NotNull
  public PsiJavaCodeReferenceElement[] getImplicitlyImportedPackageReferences() {
    return PsiImplUtil.namesToPackageReferences(myManager, IMPLICIT_IMPORTS);
  }

  public static class StaticImportFilteringProcessor implements PsiScopeProcessor {
    private final PsiScopeProcessor myDelegate;
    private String myNameToFilter;
    private boolean myIsProcessingOnDemand;
    private final HashSet<String> myHiddenNames = new HashSet<String>();

    public StaticImportFilteringProcessor(PsiScopeProcessor delegate, String nameToFilter) {
      myDelegate = delegate;
      myNameToFilter = nameToFilter;
    }

    public void setNameToFilter(String nameToFilter) {
      myNameToFilter = nameToFilter;
    }

    public <T> T getHint(Class<T> hintClass) {
      return myDelegate.getHint(hintClass);
    }

    public void handleEvent(Event event, Object associated) {
      if (JavaScopeProcessorEvent.SET_CURRENT_FILE_CONTEXT.equals(event)) {
        if (associated instanceof PsiImportStaticStatement) {
          final PsiImportStaticStatement importStaticStatement = (PsiImportStaticStatement)associated;
          if (importStaticStatement.isOnDemand()) {
            myIsProcessingOnDemand = true;
          }
          else {
            myIsProcessingOnDemand = false;
            myHiddenNames.add(importStaticStatement.getReferenceName());
          }
        }
        else {
          myIsProcessingOnDemand = false;
        }
      }

      myDelegate.handleEvent(event, associated);
    }

    public boolean execute(PsiElement element, ResolveState state) {
      if (element instanceof PsiModifierListOwner && ((PsiModifierListOwner)element).hasModifierProperty(PsiModifier.STATIC)) {
        if (myNameToFilter != null &&
            (!(element instanceof PsiNamedElement) || !myNameToFilter.equals(((PsiNamedElement)element).getName()))) {
            return true;
        }
        if (element instanceof PsiNamedElement && myIsProcessingOnDemand) {
          final String name = ((PsiNamedElement)element).getName();
          if (myHiddenNames.contains(name)) return true;
        }
        return myDelegate.execute(element, state);
      }
      else {
        return true;
      }
    }
  }

  public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place){
    if(!processDeclarationsNoGuess(processor, state, lastParent, place)){
      if(processor instanceof ClassResolverProcessor){
        final ClassResolverProcessor hint = (ClassResolverProcessor)processor;
        if(isPhysical()){
          setGuess(hint.getName(state), hint.getResult());
        }
      }
      return false;
    }
    return true;
  }

  private boolean processDeclarationsNoGuess(PsiScopeProcessor processor, ResolveState state, PsiElement lastParent, PsiElement place){
    processor.handleEvent(PsiScopeProcessor.Event.SET_DECLARATION_HOLDER, this);
    final ElementClassHint classHint = processor.getHint(ElementClassHint.class);
    final NameHint nameHint = processor.getHint(NameHint.class);
    final String name = nameHint != null ? nameHint.getName(state) : null;
    if (classHint == null || classHint.shouldProcess(PsiClass.class)){
      if(processor instanceof ClassResolverProcessor){
        // Some speedup
        final JavaResolveResult[] guessClass = getGuess(name);
        if(guessClass != null){
          ((ClassResolverProcessor) processor).forceResult(guessClass);
          return false;
        }
      }

      final PsiClass[] classes = getClasses();
      for (PsiClass aClass : classes) {
        if (!processor.execute(aClass, state)) return false;
      }

      PsiImportList importList = getImportList();
      PsiImportStatement[] importStatements = importList.getImportStatements();

      //Single-type processing
      for (PsiImportStatement statement : importStatements) {
        if (!statement.isOnDemand()) {
          if (nameHint != null) {
            String refText = statement.getQualifiedName();
            if (refText == null || !refText.endsWith(name)) continue;
          }

          PsiElement resolved = statement.resolve();
          if (resolved instanceof PsiClass) {
            processor.handleEvent(JavaScopeProcessorEvent.SET_CURRENT_FILE_CONTEXT, statement);
            if (!processor.execute(resolved, state)) return false;
          }
        }
      }
      processor.handleEvent(JavaScopeProcessorEvent.SET_CURRENT_FILE_CONTEXT, null);

      // check in current package
      String packageName = getPackageName();
      PsiPackage aPackage = JavaPsiFacade.getInstance(myManager.getProject()).findPackage(packageName);
      if (aPackage != null){
        if (!aPackage.processDeclarations(processor, state, null, place)) {
          return false;
        }
      }

      //On demand processing
      for (PsiImportStatement statement : importStatements) {
        if (statement.isOnDemand()) {
          PsiElement resolved = statement.resolve();
          if (resolved != null) {
            processor.handleEvent(JavaScopeProcessorEvent.SET_CURRENT_FILE_CONTEXT, statement);
            processOnDemandTarget(resolved, processor, state, place);
          }
        }
      }
    }

    if(classHint == null || classHint.shouldProcess(PsiPackage.class)){
      final PsiPackage rootPackage = JavaPsiFacade.getInstance(getProject()).findPackage("");
      processor.handleEvent(JavaScopeProcessorEvent.SET_CURRENT_FILE_CONTEXT, rootPackage);
      if(rootPackage != null) rootPackage.processDeclarations(processor, state, null, place);
    }

    // todo[dsl] class processing
    final PsiImportList importList = getImportList();
    final PsiImportStaticStatement[] importStaticStatements = importList.getImportStaticStatements();
    if (importStaticStatements.length > 0) {
      final StaticImportFilteringProcessor staticImportProcessor = new StaticImportFilteringProcessor(processor, null);

      // single member processing
      for (PsiImportStaticStatement importStaticStatement : importStaticStatements) {
        if (!importStaticStatement.isOnDemand()) {
          final String referenceName = importStaticStatement.getReferenceName();
          final PsiClass targetElement = importStaticStatement.resolveTargetClass();
          if (targetElement != null) {
            staticImportProcessor.setNameToFilter(referenceName);
            staticImportProcessor.handleEvent(JavaScopeProcessorEvent.SET_CURRENT_FILE_CONTEXT, importStaticStatement);
            final boolean result = targetElement.processDeclarations(staticImportProcessor, state, lastParent, place);
            if (!result) return false;
          }
        }
      }

      // on-demand processing
      for (PsiImportStaticStatement importStaticStatement : importStaticStatements) {
        if (importStaticStatement.isOnDemand()) {
          final PsiClass targetElement = importStaticStatement.resolveTargetClass();
          if (targetElement != null) {
            staticImportProcessor.setNameToFilter(null);
            staticImportProcessor.handleEvent(JavaScopeProcessorEvent.SET_CURRENT_FILE_CONTEXT, importStaticStatement);
            final boolean result = targetElement.processDeclarations(staticImportProcessor, state, lastParent, place);
            if (!result) return false;
          }
        }
      }

      staticImportProcessor.handleEvent(JavaScopeProcessorEvent.SET_CURRENT_FILE_CONTEXT, null);
    }

    if (classHint == null || classHint.shouldProcess(PsiClass.class)){
      processor.handleEvent(JavaScopeProcessorEvent.SET_CURRENT_FILE_CONTEXT, null);

      PsiJavaCodeReferenceElement[] implicitlyImported = getImplicitlyImportedPackageReferences();
      for (PsiJavaCodeReferenceElement aImplicitlyImported : implicitlyImported) {
        PsiElement resolved = aImplicitlyImported.resolve();
        if (resolved != null) {
          if (!processOnDemandTarget(resolved, processor, state, place)) return false;
        }
      }
    }

    return true;
  }

  private static boolean processOnDemandTarget (PsiElement target, PsiScopeProcessor processor, ResolveState substitutor, PsiElement place) {
    if (target instanceof PsiPackage) {
      if (!target.processDeclarations(processor, substitutor, null, place)) {
        return false;
      }
    }
    else if (target instanceof PsiClass) {
      PsiClass[] inners = ((PsiClass)target).getInnerClasses();
      for (PsiClass inner : inners) {
        if (!processor.execute(inner, substitutor)) return false;
      }
    }
    else {
      LOG.assertTrue(false);
    }
    return true;
  }

  @Nullable
  private JavaResolveResult[] getGuess(final String name){
    final Map<String,SoftReference<JavaResolveResult[]>> guessForFile = myGuessCache.get(this);
    final Reference<JavaResolveResult[]> ref = guessForFile != null ? guessForFile.get(name) : null;

    return ref != null ? ref.get() : null;
  }

  private void setGuess(final String name, final JavaResolveResult[] cached) {
    ConcurrentMap<String, SoftReference<JavaResolveResult[]>> guessForFile = myGuessCache.get(this);
    if (guessForFile == null) {
      guessForFile = ConcurrencyUtil.cacheOrGet(myGuessCache, this, new ConcurrentHashMap<String, SoftReference<JavaResolveResult[]>>());
    }
    guessForFile.putIfAbsent(name, new SoftReference<JavaResolveResult[]>(cached));
  }

  public void accept(@NotNull PsiElementVisitor visitor){
    if (visitor instanceof JavaElementVisitor) {
      ((JavaElementVisitor)visitor).visitJavaFile(this);
    }
    else {
      visitor.visitFile(this);
    }
  }

  @NotNull
  public Language getLanguage() {
    return StdLanguages.JAVA;
  }

  public boolean importClass(PsiClass aClass) {
    return JavaCodeStyleManager.getInstance(getProject()).addImport(this, aClass);
  }

  private static final Key<ParameterizedCachedValue<LanguageLevel, PsiJavaFile>> LANGUAGE_LEVEL = Key.create("LANGUAGE_LEVEL");
  @NotNull
  public LanguageLevel getLanguageLevel() {
    return getManager().getCachedValuesManager().getParameterizedCachedValue(this, LANGUAGE_LEVEL, myLanguageLevelProvider, false, this);
  }

  private LanguageLevel getLanguageLevelInner() {
    if (myOriginalFile instanceof PsiJavaFile) return ((PsiJavaFile)myOriginalFile).getLanguageLevel();
    final LanguageLevel forcedLanguageLevel = getUserData(PsiUtil.FILE_LANGUAGE_LEVEL_KEY);
    if (forcedLanguageLevel != null) return forcedLanguageLevel;
    final VirtualFile virtualFile = getVirtualFile();
    if (virtualFile == null) {
      final PsiFile originalFile = getOriginalFile();
      if (originalFile instanceof PsiJavaFile && originalFile != this) return ((PsiJavaFile)originalFile).getLanguageLevel();
      return LanguageLevelProjectExtension.getInstance(getProject()).getLanguageLevel();
    }

    final VirtualFile folder = virtualFile.getParent();
    if (folder != null) {
      final LanguageLevel level = folder.getUserData(LanguageLevel.KEY);
      if (level != null) return level;
    }

    final Project project = getProject();
    final ProjectFileIndex index = ProjectRootManager.getInstance(project).getFileIndex();
    final VirtualFile sourceRoot = index.getSourceRootForFile(virtualFile);
    if (sourceRoot != null) {
      String relativePath = VfsUtil.getRelativePath(folder, sourceRoot, '/');
      LOG.assertTrue(relativePath != null);
      final VirtualFile[] files = index.getOrderEntriesForFile(virtualFile).get(0).getFiles(OrderRootType.CLASSES);
      for (VirtualFile rootFile : files) {
        final VirtualFile classFile = rootFile.findFileByRelativePath(relativePath);
        if (classFile != null) {
          return getLanguageLevel(classFile);
        }
      }
    }

    return LanguageLevelProjectExtension.getInstance(project).getLanguageLevel();
  }

  private LanguageLevel getLanguageLevel(final VirtualFile dirFile) {
    final VirtualFile[] children = dirFile.getChildren();
    final LanguageLevel defaultLanguageLevel = LanguageLevelProjectExtension.getInstance(getProject()).getLanguageLevel();
    for (VirtualFile child : children) {
      if (StdFileTypes.CLASS.equals(child.getFileType())) {
        final PsiFile psiFile = getManager().findFile(child);
        if (psiFile instanceof PsiJavaFile) return ((PsiJavaFile)psiFile).getLanguageLevel();
      }
    }

    return defaultLanguageLevel;
  }
}