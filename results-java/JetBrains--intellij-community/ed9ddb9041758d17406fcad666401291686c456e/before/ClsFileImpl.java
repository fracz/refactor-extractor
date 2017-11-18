package com.intellij.psi.impl.compiled;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiFileEx;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.cache.RepositoryManager;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class ClsFileImpl extends ClsRepositoryPsiElement implements PsiJavaFile, PsiFileEx {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.compiled.ClsFileImpl");

  private ClsClassImpl myClass = null;
  private ClsPackageStatementImpl myPackageStatement = null;
  private static final Key<Document> DOCUMENT_IN_MIRROR_KEY = Key.create("DOCUMENT_IN_MIRROR_KEY");
  private final boolean myIsForDecompiling;
  private final FileViewProvider myViewProvider;

  private ClsFileImpl(PsiManagerImpl manager, FileViewProvider viewProvider, boolean forDecompiling) {
    super(manager, -2);
    myIsForDecompiling = forDecompiling;
    myViewProvider = viewProvider;
  }

  public ClsFileImpl(PsiManagerImpl manager, FileViewProvider viewProvider) {
    this(manager, viewProvider, false);
  }


  public boolean isContentsLoaded() {
    if (myClass == null) return false;
    return myClass.isContentsLoaded();
  }

  public void unloadContent() {
    if (myClass != null) {
      myClass.invalidate();
      myClass = null;
    }
    if (myPackageStatement != null) {
      myPackageStatement.invalidate();
      myPackageStatement = null;
    }
    myMirror = null;
  }

  public long getRepositoryId() {
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

  public boolean isRepositoryIdInitialized() {
    return super.getRepositoryId() != -2;
  }

  @NotNull
  public VirtualFile getVirtualFile() {
    return myViewProvider.getVirtualFile();
  }

  public PsiElement getParent() {
    return getContainingDirectory();
  }

  public PsiDirectory getContainingDirectory() {
    VirtualFile parentFile = getVirtualFile().getParent();
    if (parentFile == null) return null;
    return getManager().findDirectory(parentFile);
  }

  public PsiFile getContainingFile() {
    return this;
  }

  public boolean isValid() {
    if (myIsForDecompiling) return true;
    VirtualFile vFile = getVirtualFile();
    return vFile.isValid();
  }

  public String getName() {
    return getVirtualFile().getName();
  }

  @NotNull
  public PsiElement[] getChildren() {
    return getClasses(); // TODO : package statement?
  }

  @NotNull
  public PsiClass[] getClasses() {
    long id = getRepositoryId();
    if (myClass == null) {
      if (id >= 0) {
        long[] classIds = getRepositoryManager().getFileView().getClasses(id);
        LOG.assertTrue(classIds.length == 1);
        myClass = (ClsClassImpl)getRepositoryElementsManager().findOrCreatePsiElementById(classIds[0]);
      }
      else {
        myClass = new ClsClassImpl(myManager, this, getVirtualFile());
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

  public PsiImportList getImportList() {
    return null;
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

  public PsiElement setName(String name) throws IncorrectOperationException {
    throw new IncorrectOperationException(CAN_NOT_MODIFY_MESSAGE);
  }

  public void checkSetName(String name) throws IncorrectOperationException {
    throw new IncorrectOperationException(CAN_NOT_MODIFY_MESSAGE);
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

  public void setMirror(TreeElement element) {
    LOG.assertTrue(myMirror == null);
    myMirror = element;

    PsiElement mirrorFile = SourceTreeToPsiMap.treeElementToPsi(myMirror);
    if (mirrorFile instanceof PsiJavaFile) {
      PsiPackageStatement packageStatement = ((PsiJavaFile)mirrorFile).getPackageStatement();
      if (packageStatement != null) {
          ((ClsElementImpl)getPackageStatement()).setMirror((TreeElement)SourceTreeToPsiMap.psiElementToTree(packageStatement));
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

  private void initializeMirror() {
    if (myMirror == null) {
      FileDocumentManager documentManager = FileDocumentManager.getInstance();
      Document document = documentManager.getDocument(getVirtualFile());
      String text = document.getText();
      String ext = StdFileTypes.JAVA.getDefaultExtension();
      PsiClass aClass = getClasses()[0];
      String fileName = aClass.getName() + "." + ext;
      PsiManager manager = getManager();
      PsiFile mirror = manager.getElementFactory().createFileFromText(fileName, text);
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

  public void accept(PsiElementVisitor visitor) {
    visitor.visitJavaFile(this);
  }

  public String getDetectedLineSeparator() {
    throw new UnsupportedOperationException();
  }

  public String toString() {
    return "PsiFile:" + getName();
  }

  public PsiFile getOriginalFile() {
    return null;
  }

  public boolean canContainJavaCode() {
    return true;
  }

  @NotNull
  public FileType getFileType() {
    return StdFileTypes.CLASS;
  }

  @NotNull
  public PsiFile[] getPsiRoots() {
    return new PsiFile[]{this};
  }

  public FileViewProvider getViewProvider() {
    return myViewProvider;
  }

  public static String decompile(PsiManager manager, VirtualFile file) {
    final ClsFileImpl psiFile = new ClsFileImpl((PsiManagerImpl)manager, new SingleRootFileViewProvider(manager, file), true);
    StringBuffer buffer = new StringBuffer();
    psiFile.appendMirrorText(0, buffer);
    return buffer.toString();
  }
}