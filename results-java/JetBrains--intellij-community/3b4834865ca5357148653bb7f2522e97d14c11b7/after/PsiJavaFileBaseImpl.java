package com.intellij.psi.impl.source;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiImplUtil;
import com.intellij.psi.impl.source.resolve.ClassResolverProcessor;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.impl.source.tree.ChildRole;
import com.intellij.psi.scope.ElementClassHint;
import com.intellij.psi.scope.NameHint;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.scope.util.PsiScopesUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.HashMap;
import com.intellij.util.containers.HashSet;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class PsiJavaFileBaseImpl extends PsiFileImpl implements PsiJavaFile {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.source.PsiJavaFileBaseImpl");
  private static final Key CACHED_CLASSES_MAP_KEY = Key.create("PsiJavaFileBaseImpl.CACHED_CLASSES_MAP_KEY");

  private PsiImportListImpl myRepositoryImportList = null;
  private String myCachedPackageName = null;
  protected PsiClass[] myCachedClasses = null;
  private final Map<PsiJavaFile,Map<String, SoftReference<JavaResolveResult[]>>> myGuessCache = ResolveCache.getOrCreateWeakMap(myManager, CACHED_CLASSES_MAP_KEY, false);

  private static final String[] IMPLICIT_IMPORTS = new String[]{ "java.lang" };

  protected PsiJavaFileBaseImpl(Project project,
                                IElementType elementType,
                                IElementType contentElementType,
                                String name,
                                CharSequence text) {
    super(project, elementType, contentElementType, name, text);
  }

  protected PsiJavaFileBaseImpl(Project project, IElementType elementType, IElementType contentElementType, VirtualFile file) {
    super(project, elementType, contentElementType, file);
  }

  public void subtreeChanged() {
    super.subtreeChanged();
    myCachedPackageName = null;
    myCachedClasses = null;
  }

  protected PsiJavaFileBaseImpl clone() {
    PsiJavaFileBaseImpl clone = (PsiJavaFileBaseImpl)super.clone();
    clone.myRepositoryImportList = null;
    clone.myCachedClasses = null;
    return clone;
  }

  public void setRepositoryId(long repositoryId) {
    super.setRepositoryId(repositoryId);

    if (repositoryId < 0){
      if (myRepositoryImportList != null){
        myRepositoryImportList.setOwner(this);
        myRepositoryImportList = null;
      }
    }
    else{
      myRepositoryImportList = (PsiImportListImpl)bindSlave(ChildRole.IMPORT_LIST);
    }
  }

  public PsiClass[] getClasses() {
    if (myCachedClasses == null){
      if (getTreeElement() != null || getRepositoryId() < 0){
        LOG.debug("Loading tree for " + getName());
        myCachedClasses = (PsiClass[])calcTreeElement().getChildrenAsPsiElements(CLASS_BIT_SET, PSI_CLASS_ARRAY_CONSTRUCTOR);
      }
      else{
        long[] classIds = getRepositoryManager().getFileView().getClasses(getRepositoryId());
        PsiClass[] classes = classIds.length > 0 ? new PsiClass[classIds.length] : PsiClass.EMPTY_ARRAY;
        for(int i = 0; i < classIds.length; i++){
          long id = classIds[i];
          classes[i] = (PsiClass)getRepositoryElementsManager().findOrCreatePsiElementById(id);
        }
        myCachedClasses = classes;
      }
    }
    return myCachedClasses;
  }

  public PsiPackageStatement getPackageStatement() {
    return (PsiPackageStatement)calcTreeElement().findChildByRoleAsPsiElement(ChildRole.PACKAGE_STATEMENT);
  }

  public String getPackageName(){
    if (myCachedPackageName == null){
      if (getTreeElement() != null || getRepositoryId() < 0){
        PsiPackageStatement statement = getPackageStatement();
        if (statement == null){
          myCachedPackageName = "";
        }
        else{
          myCachedPackageName = statement.getPackageName();
        }
      }
      else{
        myCachedPackageName = getRepositoryManager().getFileView().getPackageName(getRepositoryId());
      }
    }
    return myCachedPackageName;
  }

  public PsiImportList getImportList() {
    if (getRepositoryId() >= 0){
      if (myRepositoryImportList == null){
        myRepositoryImportList = new PsiImportListImpl(myManager, this);
      }
      return myRepositoryImportList;
    }
    else{
      return (PsiImportList)calcTreeElement().findChildByRoleAsPsiElement(ChildRole.IMPORT_LIST);
    }
  }

  public PsiElement[] getOnDemandImports(boolean includeImplicit, boolean checkIncludes) {
    List<PsiElement> array = new ArrayList<PsiElement>();

    PsiImportList importList = getImportList();
    PsiImportStatement[] statements = importList.getImportStatements();
    for(int i = 0; i < statements.length; i++) {
      PsiImportStatement statement = statements[i];
      if (statement.isOnDemand()){
        PsiElement resolved = statement.resolve();
        if (resolved != null){
          array.add(resolved);
        }
      }
    }

    if (includeImplicit){
      PsiJavaCodeReferenceElement[] implicitRefs = getImplicitlyImportedPackageReferences();
      for(int i = 0; i < implicitRefs.length; i++){
        final PsiElement resolved = implicitRefs[i].resolve();
        if (resolved != null) {
          array.add(resolved);
        }
      }
    }

    return array.toArray(new PsiElement[array.size()]);
  }

  public PsiClass[] getSingleClassImports(boolean checkIncludes) {
    List<PsiClass> array = new ArrayList<PsiClass>();
    PsiImportList importList = getImportList();
    PsiImportStatement[] statements = importList.getImportStatements();
    for(int i = 0; i < statements.length; i++) {
      PsiImportStatement statement = statements[i];
      if (!statement.isOnDemand()){
        PsiElement ref = statement.resolve();
        if (ref instanceof PsiClass){
          array.add((PsiClass)ref);
        }
      }
    }
    return array.toArray(new PsiClass[array.size()]);
  }

  public PsiJavaCodeReferenceElement findImportReferenceTo(PsiClass aClass) {
    PsiImportList importList = getImportList();
    PsiImportStatement[] statements = importList.getImportStatements();
    for(int i = 0; i < statements.length; i++) {
      PsiImportStatement statement = statements[i];
      if (!statement.isOnDemand()){
        PsiElement ref = statement.resolve();
        if (ref != null && getManager().areElementsEquivalent(ref, aClass)){
          return statement.getImportReference();
        }
      }
    }
    return null;
  }

  public String[] getImplicitlyImportedPackages() {
    return IMPLICIT_IMPORTS;
  }

  public PsiJavaCodeReferenceElement[] getImplicitlyImportedPackageReferences() {
    return PsiImplUtil.namesToPackageReferences(myManager, IMPLICIT_IMPORTS);
  }

  public boolean canContainJavaCode() {
    return true;
  }

  public static class StaticImportFilteringProcessor implements PsiScopeProcessor {
    private final PsiScopeProcessor myDelegate;
    private String myNameToFilter;
    private boolean myIsProcessingOnDemand;
    private HashSet<String> myHiddenNames = new HashSet<String>();

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
      if (Event.SET_CURRENT_FILE_CONTEXT.equals(event)) {
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

    public boolean execute(PsiElement element, PsiSubstitutor substitutor) {
      if (element instanceof PsiModifierListOwner && ((PsiModifierListOwner)element).hasModifierProperty(PsiModifier.STATIC)) {
        if (myNameToFilter != null &&
            (!(element instanceof PsiNamedElement) || !myNameToFilter.equals(((PsiNamedElement)element).getName()))) {
            return true;
        }
        if (element instanceof PsiNamedElement && myIsProcessingOnDemand) {
          final String name = ((PsiNamedElement)element).getName();
          if (myHiddenNames.contains(name)) return true;
        }
        return myDelegate.execute(element, substitutor);
      }
      else {
        return true;
      }
    }
  }

  public boolean processDeclarations(PsiScopeProcessor processor, PsiSubstitutor substitutor, PsiElement lastParent, PsiElement place){
    if(!processDeclarationsNoGuess(processor, substitutor, lastParent, place)){
      if(processor instanceof ClassResolverProcessor){
        final ClassResolverProcessor hint = (ClassResolverProcessor)processor;
        if(isPhysical()){
          setGuess(hint.getName(), hint.getResult());
        }
      }
      return false;
    }
    return true;
  }

  private boolean processDeclarationsNoGuess(PsiScopeProcessor processor, PsiSubstitutor substitutor, PsiElement lastParent, PsiElement place){
    processor.handleEvent(PsiScopeProcessor.Event.SET_DECLARATION_HOLDER, this);
    final ElementClassHint classHint = processor.getHint(ElementClassHint.class);
    final NameHint nameHint = processor.getHint(NameHint.class);
    final String name = nameHint != null ? nameHint.getName() : null;
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
      for(int i = 0; i < classes.length; i++){
        PsiClass aClass = classes[i];
        if (!processor.execute(aClass, substitutor)) return false;
      }

      PsiImportList importList = getImportList();
      PsiImportStatement[] importStatements = importList.getImportStatements();

      //Single-type processing
      for(int i1 = 0; i1 < importStatements.length; i1++) {
        PsiImportStatement statement = importStatements[i1];
        if (!statement.isOnDemand()){
          if (nameHint != null) {
            String refText = statement.getQualifiedName();
            if (refText == null || !refText.endsWith(name)) continue;
          }

          PsiElement resolved = statement.resolve();
          if (resolved instanceof PsiClass){
            processor.handleEvent(PsiScopeProcessor.Event.SET_CURRENT_FILE_CONTEXT, statement);
            if (!processor.execute(resolved, substitutor)) return false;
          }
        }
      }
      processor.handleEvent(PsiScopeProcessor.Event.SET_CURRENT_FILE_CONTEXT, null);

      {
        // check in current package
        String packageName = getPackageName();
        PsiPackage aPackage = myManager.findPackage(packageName);
        if (aPackage != null){
          if(!PsiScopesUtil.processScope(aPackage, processor, substitutor, null, place))
            return false;
        }
      }

      //On demand processing
      for (int i = 0; i < importStatements.length; i++) {
        PsiImportStatement statement = importStatements[i];
        if (statement.isOnDemand()) {
          PsiElement resolved = statement.resolve();
          if (resolved != null) {
            processor.handleEvent(PsiScopeProcessor.Event.SET_CURRENT_FILE_CONTEXT, statement);
            processOnDemandTarget(resolved, processor, substitutor, place);
          }
        }
      }
      processor.handleEvent(PsiScopeProcessor.Event.SET_CURRENT_FILE_CONTEXT, null);

      PsiJavaCodeReferenceElement[] implicitlyImported = getImplicitlyImportedPackageReferences();
      for (int i = 0; i < implicitlyImported.length; i++) {
        PsiElement resolved = implicitlyImported[i].resolve();
        if (resolved != null) {
          processOnDemandTarget(resolved, processor, substitutor, place);
        }
      }
    }

    if(classHint == null || classHint.shouldProcess(PsiPackage.class)){
      final PsiPackage rootPackage = getManager().findPackage("");
      if(rootPackage != null) rootPackage.processDeclarations(processor, substitutor, null, place);
    }


    // todo[dsl] class processing
    final PsiImportList importList = getImportList();
    if (importList != null) {
      final PsiImportStaticStatement[] importStaticStatements = importList.getImportStaticStatements();
      if (importStaticStatements.length > 0) {
        final StaticImportFilteringProcessor staticImportProcessor = new StaticImportFilteringProcessor(processor, null);

        // single member processing
        for (int i = 0; i < importStaticStatements.length; i++) {
          PsiImportStaticStatement importStaticStatement = importStaticStatements[i];
          if (!importStaticStatement.isOnDemand()) {
            final String referenceName = importStaticStatement.getReferenceName();
            final PsiClass targetElement = importStaticStatement.resolveTargetClass();
            if (targetElement != null) {
              staticImportProcessor.setNameToFilter(referenceName);
              staticImportProcessor.handleEvent(PsiScopeProcessor.Event.SET_CURRENT_FILE_CONTEXT, importStaticStatement);
              final boolean result = targetElement.processDeclarations(staticImportProcessor, substitutor, lastParent, place);
              if (!result) return false;
            }
          }
        }

        // on-demand processing
        for (int i = 0; i < importStaticStatements.length; i++) {
          PsiImportStaticStatement importStaticStatement = importStaticStatements[i];
          if (importStaticStatement.isOnDemand()) {
            final PsiClass targetElement = importStaticStatement.resolveTargetClass();
            if (targetElement != null) {
              staticImportProcessor.setNameToFilter(null);
              staticImportProcessor.handleEvent(PsiScopeProcessor.Event.SET_CURRENT_FILE_CONTEXT, importStaticStatement);
              staticImportProcessor.handleEvent(PsiScopeProcessor.Event.BEGIN_GROUP, null);
              final boolean result = targetElement.processDeclarations(staticImportProcessor, substitutor, lastParent, place);
              staticImportProcessor.handleEvent(PsiScopeProcessor.Event.END_GROUP, null);
              if (!result) return false;
            }
          }
        }

        staticImportProcessor.handleEvent(PsiScopeProcessor.Event.SET_CURRENT_FILE_CONTEXT, null);
      }
    }

    return true;
  }

  private static boolean processOnDemandTarget (PsiElement target, PsiScopeProcessor processor, PsiSubstitutor substitutor, PsiElement place) {
    if (target instanceof PsiPackage) {
      processor.handleEvent(PsiScopeProcessor.Event.BEGIN_GROUP, null);
      if (!PsiScopesUtil.processScope(target, processor, substitutor, null, place)) {
        return false;
      }
      processor.handleEvent(PsiScopeProcessor.Event.END_GROUP, null);
    }
    else if (target instanceof PsiClass) {
      PsiClass[] inners = ((PsiClass)target).getInnerClasses();
      for (int j = 0; j < inners.length; j++) {
        PsiClass inner = inners[j];
        if (!processor.execute(inner, substitutor)) return false;
      }
    }
    else {
      LOG.assertTrue(false);
    }
    return true;
  }

  private JavaResolveResult[] getGuess(final String name){
    final Map guessForFile = myGuessCache.get(this);
    final Reference ref = guessForFile != null ? (Reference)guessForFile.get(name) : null;

    return ref != null ? (JavaResolveResult[])ref.get() : null;
  }

  private void setGuess(final String name, final JavaResolveResult[] cached){
    Map<String,SoftReference<JavaResolveResult[]>> guessForFile = myGuessCache.get(this);
    if(guessForFile == null){
      guessForFile = new HashMap<String, SoftReference<JavaResolveResult[]>>();
      myGuessCache.put(this, guessForFile);
    }
    guessForFile.put(name, new SoftReference<JavaResolveResult[]>(cached));
  }


  public void accept(PsiElementVisitor visitor){
    visitor.visitJavaFile(this);
  }
}