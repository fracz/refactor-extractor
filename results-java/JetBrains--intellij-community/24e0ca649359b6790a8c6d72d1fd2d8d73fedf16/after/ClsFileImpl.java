package com.intellij.psi.impl.compiled;

import com.intellij.ide.startup.FileContent;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageDialect;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiFileEx;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.cache.RepositoryManager;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.cls.BytePointer;
import com.intellij.util.cls.ClsFormatException;
import com.intellij.util.cls.ClsUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class ClsFileImpl extends ClsRepositoryPsiElement implements PsiJavaFile, PsiFileEx {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.compiled.ClsFileImpl");

  private volatile ClsClassImpl myClass = null;
  private volatile ClsPackageStatementImpl myPackageStatement = null;
  private static final Key<Document> DOCUMENT_IN_MIRROR_KEY = Key.create("DOCUMENT_IN_MIRROR_KEY");
  private final boolean myIsForDecompiling;
  private final FileViewProvider myViewProvider;
  private volatile LanguageLevel myLanguageLevel = null;
  private volatile boolean myContentsUnloaded;
  private static final int MAX_CLASS_FILE_MAJOR_VERSION = 50;
  private FileContent myContent = null; // For copies created by CacheUtil only

  private ClsFileImpl(@NotNull PsiManagerImpl manager, @NotNull FileViewProvider viewProvider, boolean forDecompiling) {
    super(manager, -2);
    myIsForDecompiling = forDecompiling;
    myViewProvider = viewProvider;
  }

  public ClsFileImpl(PsiManagerImpl manager, FileViewProvider viewProvider) {
    this(manager, viewProvider, false);
  }

  public void setContent(final FileContent content) {
    myContent = content;
  }

  public boolean isContentsLoaded() {
    return myClass != null && myClass.isContentsLoaded();
  }

  public void unloadContent() {
    myLanguageLevel = null;
    if (myClass != null) {
      myClass.invalidate();
      myClass = null;
    }
    if (myPackageStatement != null) {
      myPackageStatement.invalidate();
      myPackageStatement = null;
    }
    myMirror = null;
    myContentsUnloaded = true;
  }

  public long getRepositoryId() {
    synchronized (PsiLock.LOCK) {
      long id = super.getRepositoryId();
      if (id == -2) {
        RepositoryManager repositoryManager = getRepositoryManager();
        if (repositoryManager != null) {
          id = repositoryManager.getFileId(getVirtualFile());
        }
        else {
          id = -1;
        }

        super.setRepositoryId(id);
      }
      return id;
    }
  }

  public boolean isRepositoryIdInitialized() {
    return super.getRepositoryId() != -2;
  }

  @NotNull
  public VirtualFile getVirtualFile() {
    return myViewProvider.getVirtualFile();
  }

  public boolean processChildren(final PsiElementProcessor<PsiFileSystemItem> processor) {
    return true;
  }

  public PsiDirectory getParent() {
    return getContainingDirectory();
  }

  public PsiDirectory getContainingDirectory() {
    VirtualFile parentFile = getVirtualFile().getParent();
    if (parentFile == null) return null;
    return getManager().findDirectory(parentFile);
  }

  public PsiFile getContainingFile() {
    if (!isValid()) throw new PsiInvalidElementAccessException(this);
    return this;
  }

  public boolean isValid() {
    if (myIsForDecompiling) return true;
    VirtualFile vFile = getVirtualFile();
    return vFile.isValid();
  }

  @NotNull
  public String getName() {
    return getVirtualFile().getName();
  }

  @NotNull
  public PsiElement[] getChildren() {
    return getClasses(); // TODO : package statement?
  }

  @Nullable
  public ClsClassImpl getCachedClass() {
    return myClass;
  }

  @NotNull
  public PsiClass[] getClasses() {
    long id = getRepositoryId();
    if (myClass == null) {
      if (id >= 0 && !myContentsUnloaded) {
        long[] classIds = getRepositoryManager().getFileView().getClasses(id);
        LOG.assertTrue(classIds.length == 1, "Wrong number of compiled classes in repository: " + classIds.length);
        myClass = (ClsClassImpl)getRepositoryElementsManager().findOrCreatePsiElementById(classIds[0]);
      }
      else {
        byte[] bytes = null;
        if (myContent != null) {
          try {
            bytes = myContent.getBytes();
          }
          catch (IOException e) {
            // ignore
          }
        }

        final ClassFileData fileData = bytes != null ? new ClassFileData(bytes, getVirtualFile()) : new ClassFileData(getVirtualFile());
        myClass = new ClsClassImpl(myManager, this, fileData);
        myContentsUnloaded = false;
      }
    }
    return new PsiClass[]{myClass};
  }

  public PsiPackageStatement getPackageStatement() {
    if (myPackageStatement == null) {
      myPackageStatement = new ClsPackageStatementImpl(this);
    }
    return myPackageStatement.getPackageName() != null ? myPackageStatement : null;
  }

  @NotNull
  public String getPackageName() {
    PsiPackageStatement statement = getPackageStatement();
    if (statement == null) {
      return "";
    }
    else {
      return statement.getPackageName();
    }
  }

  public void setPackageName(final String packageName) throws IncorrectOperationException {
    throw new IncorrectOperationException("Cannot set package name for compiled files");
  }

  public PsiImportList getImportList() {
    return null;
  }

  public boolean importClass(PsiClass aClass) {
    throw new UnsupportedOperationException("Cannot add imports to compiled classes");
  }

  @NotNull
  public PsiElement[] getOnDemandImports(boolean includeImplicit, boolean checkIncludes) {
    return PsiJavaCodeReferenceElement.EMPTY_ARRAY;
  }

  @NotNull
  public PsiClass[] getSingleClassImports(boolean checkIncludes) {
    return PsiClass.EMPTY_ARRAY;
  }

  @NotNull
  public String[] getImplicitlyImportedPackages() {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  @NotNull
  public PsiJavaCodeReferenceElement[] getImplicitlyImportedPackageReferences() {
    return PsiJavaCodeReferenceElement.EMPTY_ARRAY;
  }

  public PsiJavaCodeReferenceElement findImportReferenceTo(PsiClass aClass) {
    return null;
  }

  @NotNull
  public LanguageLevel getLanguageLevel() {
    //TODO: repository for language level
    if (myLanguageLevel == null) {
      myLanguageLevel = getLanguageLevelInner();
    }
    return myLanguageLevel;
  }

  private LanguageLevel getLanguageLevelInner() {
    final LanguageLevel defaultLevel = JavaPsiFacade.getInstance(getManager().getProject()).getEffectiveLanguageLevel();
    if (myClass != null && myClass.isContentsLoaded()) {
      final ClassFileData data = myClass.getClassFileData();
      if (data != null) {
        return getLanguageLevelFromClassFileData(data, defaultLevel);
      }
    }
    return getLanguageLevel(getVirtualFile(), defaultLevel);
  }

  public static LanguageLevel getLanguageLevel(VirtualFile vFile, LanguageLevel defaultLanguageLevel) {
    final ClassFileData classFileData = new ClassFileData(vFile);
    return getLanguageLevelFromClassFileData(classFileData, defaultLanguageLevel);
  }

  private static LanguageLevel getLanguageLevelFromClassFileData(final ClassFileData classFileData, final LanguageLevel defaultLanguageLevel) {
    try {
      final BytePointer ptr = new BytePointer(classFileData.getData(), 6);
      int majorVersion = ClsUtil.readU2(ptr);
      if (majorVersion < MAX_CLASS_FILE_MAJOR_VERSION) {
        /*check if the class file was processed by retroweaver, it has written an attribute of format
          (int retroweaver build number, int original class file version, long timestamp of weaving) */
        final int attributesOffset = classFileData.getOffsetOfAttributesSection();
        final BytePointer attrPtr = classFileData.findAttribute(attributesOffset, "net.sourceforge.Retroweaver");
        if (attrPtr != null) {
          ptr.offset += 4; //skip retroweaver build number
          majorVersion = ClsUtil.readU2(ptr);
        }

      }
      return calcLanguageLevelBasedOnVersion(majorVersion);
    }
    catch (ClsFormatException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(e);
      }
      return defaultLanguageLevel;
    }
  }

  private static LanguageLevel calcLanguageLevelBasedOnVersion(final int majorVersion) {
    if (majorVersion < 48) return LanguageLevel.JDK_1_3;
    if (majorVersion < 49) return LanguageLevel.JDK_1_4;
    return LanguageLevel.JDK_1_5;
  }

  public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
    throw new IncorrectOperationException(CAN_NOT_MODIFY_MESSAGE);
  }

  public void checkSetName(String name) throws IncorrectOperationException {
    throw new IncorrectOperationException(CAN_NOT_MODIFY_MESSAGE);
  }

  public boolean isDirectory() {
    return false;
  }

  public void appendMirrorText(final int indentLevel, final StringBuffer buffer) {
    buffer.append(PsiBundle.message("psi.decompiled.text.header"));
    goNextLine(indentLevel, buffer);
    goNextLine(indentLevel, buffer);
    final PsiPackageStatement packageStatement = getPackageStatement();
    if (packageStatement != null) {
      ((ClsElementImpl)packageStatement).appendMirrorText(0, buffer);
      goNextLine(indentLevel, buffer);
      goNextLine(indentLevel, buffer);
    }
    PsiClass aClass = getClasses()[0];
    ((ClsClassImpl)aClass).appendMirrorText(0, buffer);
  }

  public void setMirror(@NotNull TreeElement element) {
    LOG.assertTrue(!CHECK_MIRROR_ENABLED || myMirror == null);
    myMirror = element;

    PsiElement mirrorFile = SourceTreeToPsiMap.treeElementToPsi(myMirror);
    if (mirrorFile instanceof PsiJavaFile) {
      PsiPackageStatement packageStatementMirror = ((PsiJavaFile)mirrorFile).getPackageStatement();
      final PsiPackageStatement packageStatement = getPackageStatement();
      if (packageStatementMirror != null && packageStatement != null) {
        ((ClsElementImpl)packageStatement).setMirror((TreeElement)SourceTreeToPsiMap.psiElementToTree(packageStatementMirror));
      }
      PsiClass[] classes = getClasses();
      PsiClass[] mirrorClasses = ((PsiJavaFile)mirrorFile).getClasses();
      LOG.assertTrue(classes.length == mirrorClasses.length);
      if (classes.length == mirrorClasses.length) {
        for (int i = 0; i < classes.length; i++) {
          ((ClsElementImpl)classes[i]).setMirror((TreeElement)SourceTreeToPsiMap.psiElementToTree(mirrorClasses[i]));
        }
      }
    }
  }

  public String getText() {
    initializeMirror();
    return myMirror.getText();
  }

  @NotNull
  public char[] textToCharArray() {
    initializeMirror();
    return myMirror.textToCharArray();
  }

    public PsiElement getNavigationElement() {
      String packageName = getPackageName();
      String sourceFileName = myClass.getSourceFileName();
      String relativeFilePath = packageName.length() == 0 ?
          sourceFileName :
          packageName.replace('.', '/') + '/' + sourceFileName;

      final VirtualFile vFile = getContainingFile().getVirtualFile();
      ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(getProject()).getFileIndex();
      final List<OrderEntry> orderEntries = projectFileIndex.getOrderEntriesForFile(vFile);
      for (OrderEntry orderEntry : orderEntries) {
        VirtualFile[] files = orderEntry.getFiles(OrderRootType.SOURCES);
        for (VirtualFile file : files) {
          VirtualFile source = file.findFileByRelativePath(relativeFilePath);
          if (source != null) {
            PsiFile psiSource = getManager().findFile(source);
            if (psiSource instanceof PsiClassOwner) {
              return psiSource;
            }
          }
        }
      }
      return this;
    }

    private void initializeMirror() {
      if (myMirror == null) {
        FileDocumentManager documentManager = FileDocumentManager.getInstance();
        Document document = documentManager.getDocument(getVirtualFile());
        String text = document.getText();
        String ext = StdFileTypes.JAVA.getDefaultExtension();
        PsiClass aClass = getClasses()[0];
        String fileName = aClass.getName() + "." + ext;
        PsiManager manager = getManager();
        PsiFile mirror = PsiFileFactory.getInstance(manager.getProject()).createFileFromText(fileName, text);
          ASTNode mirrorTreeElement = SourceTreeToPsiMap.psiElementToTree(mirror);

        //IMPORTANT: do not take lock too early - FileDocumentManager.getInstance().saveToString() can run write action...
        synchronized (PsiLock.LOCK) {
          if (myMirror == null) {
            setMirror((TreeElement)mirrorTreeElement);
            myMirror.putUserData(DOCUMENT_IN_MIRROR_KEY, document);
          }
        }
      }
    }

  public long getModificationStamp() {
    return getVirtualFile().getModificationStamp();
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JavaElementVisitor) {
      ((JavaElementVisitor)visitor).visitJavaFile(this);
    }
    else {
      visitor.visitFile(this);
    }
  }

  @NonNls
  public String toString() {
    return "PsiFile:" + getName();
  }

  public PsiFile getOriginalFile() {
    return null;
  }

  @NotNull
  public FileType getFileType() {
    return StdFileTypes.CLASS;
  }

  @NotNull
  public PsiFile[] getPsiRoots() {
    return new PsiFile[]{this};
  }

  @NotNull
  public FileViewProvider getViewProvider() {
    return myViewProvider;
  }

  public void subtreeChanged() {
  }

  public static String decompile(PsiManager manager, VirtualFile file) {
    final FileViewProvider provider = ((PsiManagerImpl)manager).getFileManager().findViewProvider(file);
    ClsFileImpl psiFile = null;
    if (provider != null) {
      psiFile = (ClsFileImpl)provider.getPsi(provider.getBaseLanguage());
    }

    if (psiFile == null) {
      psiFile = new ClsFileImpl((PsiManagerImpl)manager, new SingleRootFileViewProvider(manager, file), true);
    }

    StringBuffer buffer = new StringBuffer();
    psiFile.appendMirrorText(0, buffer);
    return buffer.toString();
  }

  @Nullable
  public LanguageDialect getLanguageDialect() {
    return null;
  }
}