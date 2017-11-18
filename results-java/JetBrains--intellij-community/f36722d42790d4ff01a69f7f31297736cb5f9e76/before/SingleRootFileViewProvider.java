/*
 * Copyright (c) 2005 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.psi;

import com.intellij.ide.highlighter.JavaClassFileType;
import com.intellij.lang.Language;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.ex.dummy.DummyFileSystem;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.SharedPsiElementImplUtil;
import com.intellij.psi.impl.compiled.ClsFileImpl;
import com.intellij.psi.impl.file.PsiBinaryFileImpl;
import com.intellij.psi.impl.file.impl.FileManagerImpl;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.impl.source.PsiPlainTextFileImpl;
import com.intellij.testFramework.MockVirtualFile;
import com.intellij.util.text.CharArrayCharSequence;
import com.intellij.util.LocalTimeCounter;
import com.intellij.util.ui.tree.TreeUtil;
import com.intellij.lexer.Lexer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.lang.ref.WeakReference;

public class SingleRootFileViewProvider implements FileViewProvider {
  private static final Logger LOG = Logger.getInstance("#" + SingleRootFileViewProvider.class.getCanonicalName());
  private final PsiManager myManager;
  private final VirtualFile myFile;
  private final boolean myPhysical;
  private PsiFile myPsiFile = null;
  private Content myContent;
  private WeakReference<Document> myDocument;
  private boolean myInSynchronization;

  public SingleRootFileViewProvider(PsiManager manager, VirtualFile file) {
    this(manager, file, true);
  }

  public SingleRootFileViewProvider(PsiManager manager, final VirtualFile virtualFile, final boolean physical) {
    myManager = manager;
    myFile = virtualFile;
    myPhysical = physical;
    setContent(new VirtualFileContent());
  }

  public Language getBaseLanguage() {
    final FileType fileType = getVirtualFile().getFileType();
    if(fileType instanceof LanguageFileType){
      final LanguageFileType languageFileType = (LanguageFileType)fileType;
      return languageFileType.getLanguage();
    }
    return Language.ANY;
  }

  public Set<Language> getRelevantLanguages() {
    return Collections.singleton(getBaseLanguage());
  }

  public final synchronized PsiFile getPsi(Language target) {
    if (!isPhysical()) {
      ((PsiManagerImpl)myManager).getFileManager().setViewProvider(getVirtualFile(), this);
    }
    return getPsiInner(target);
  }

  protected PsiFile getPsiInner(final Language target) {
    if (target != getBaseLanguage()) {
      return null;
    }
    return myPsiFile != null ? myPsiFile : (myPsiFile = createFile());
  }

  public void beforeContentsSynchronized() {
    myInSynchronization = true;
  }

  public void contentsSynchronized() {
    myInSynchronization = false;
    unsetPsiContent();
  }

  private void unsetPsiContent() {
    if(!(myContent instanceof PsiFileContent)) return;
    final Document cachedDocument = getCachedDocument();
    if (cachedDocument != null) {
      setContent(new DocumentContent());
    }
    else {
      setContent(new VirtualFileContent());
    }
  }

  public void beforeDocumentChanged() {
    final PsiFileImpl psiFile = (PsiFileImpl)getCachedPsi(getBaseLanguage());
    if(psiFile != null && psiFile.isContentsLoaded() && getContent() instanceof DocumentContent)
      setContent(new PsiFileContent(psiFile, getModificationStamp()));
  }

  public void rootChanged(PsiFile psiFile) {
    if (((PsiFileImpl)psiFile).isContentsLoaded()) {
      setContent(new PsiFileContent((PsiFileImpl)psiFile, LocalTimeCounter.currentTime()));
    }
  }

  public boolean isEventSystemEnabled() {
    return myPhysical;
  }

  public boolean isPhysical() {
    return !(getVirtualFile() instanceof MockVirtualFile) && !(getVirtualFile().getFileSystem() instanceof DummyFileSystem) && isEventSystemEnabled();
  }

  public long getModificationStamp() {
    return getContent().getModificationStamp();
  }


  public synchronized PsiFile getCachedPsi(Language target) {
    return myPsiFile;
  }

  protected PsiFile createFile() {
    final VirtualFile vFile = getVirtualFile();

    try {
      if (vFile.isDirectory()) return null;
      final FileTypeManager fileTypeManager = FileTypeManager.getInstance();
      final String name = getVirtualFile().getName();
      if (fileTypeManager.isFileIgnored(name)) return null; // cannot use ProjectFileIndex because of "name"!

      final Project project = myManager.getProject();
      final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();

      if(isPhysical()){ // check directories consistency
        final VirtualFile parent = vFile.getParent();
        if (parent == null) return null;
        final PsiDirectory psiDir = getManager().findDirectory(parent);
        if (psiDir == null) return null;
      }

      FileType fileType = getRealFileType();

      if (fileType instanceof LanguageFileType) {
        final Language language = ((LanguageFileType)fileType).getLanguage();
        if (language == StdLanguages.JAVA || vFile == null || !isTooLarge(vFile)) {
          final PsiFile psiFile = createFile(language);
          if(psiFile != null) return psiFile;
        }
      }

      if (fileType instanceof JavaClassFileType) {
        ProjectFileIndex fileIndex = projectFileIndex;
        if (fileIndex.isInLibraryClasses(vFile)) {
          // skip inners & anonymous
          int dotIndex = name.lastIndexOf('.');
          if (dotIndex < 0) dotIndex = name.length();
          int index = name.lastIndexOf('$', dotIndex);
          if (index >= 0) return null;

          return new ClsFileImpl((PsiManagerImpl)PsiManager.getInstance(project), this);
        }
        return null;
      }

      if (fileType.isBinary()) {
        return new PsiBinaryFileImpl((PsiManagerImpl)getManager(), this);
      }
      return new PsiPlainTextFileImpl(this);
    }
    catch (Throwable e) {
      LOG.error(e);
      return null;
    }
  }

  private FileType getRealFileType() {
    FileType fileType = getVirtualFile().getFileType();
    if(!isPhysical()) return fileType;
    final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(getManager().getProject()).getFileIndex();
    if(fileType == StdFileTypes.JAVA && !projectFileIndex.isInSource(getVirtualFile())) fileType = StdFileTypes.PLAIN_TEXT;
    return fileType;
  }

  private static boolean isTooLarge(final VirtualFile vFile) {
    if (FileManagerImpl.MAX_INTELLISENSE_FILESIZE == -1) return false;
    return vFile.getLength() > FileManagerImpl.MAX_INTELLISENSE_FILESIZE;
  }

  protected PsiFile createFile(Language lang){
    if(lang != getBaseLanguage()) return null;
    final ParserDefinition parserDefinition = lang.getParserDefinition();
    if (parserDefinition != null) {
      return parserDefinition.createFile(this);
    }
    return null;
  }

  public PsiManager getManager() {
    return myManager;
  }

  @NotNull
  public synchronized CharSequence getContents() {
    return getContent().getText();
  }

  @NotNull
  public VirtualFile getVirtualFile() {
    return myFile;
  }

  private Document getCachedDocument() {
    final Document document = myDocument != null ? myDocument.get() : null;
    if (document != null) return document;
    return FileDocumentManager.getInstance().getCachedDocument(getVirtualFile());
  }

  public Document getDocument() {
    Document document = myDocument != null ? myDocument.get() : null;
    if(document == null) {
      document = FileDocumentManager.getInstance().getDocument(getVirtualFile());
      myDocument = new WeakReference<Document>(document);
    }
    if(document != null && getContent() instanceof VirtualFileContent){
      setContent(new DocumentContent());
    }
    return document;
  }

  public FileViewProvider clone(){
    final SingleRootFileViewProvider clone =
      new SingleRootFileViewProvider(getManager(),
                                     new MockVirtualFile(getVirtualFile().getName(),
                                                         getRealFileType(),
                                                         getContents(),
                                                         getModificationStamp()),
                                     false);
    return clone;
  }

  public PsiReference findReferenceAt(final int offset) {
    final PsiFileImpl psiFile = (PsiFileImpl)getPsi(getBaseLanguage());
    return findReferenceAt(psiFile, offset);
  }

  public PsiElement findElementAt(final int offset, final Language language) {
    final PsiFile psiFile = getPsi(language);
    return psiFile != null ? findElementAt(psiFile, offset) : null;
  }

  public PsiReference findReferenceAt(final int offset, final Language language) {
    final PsiFile psiFile = getPsi(language);
    return psiFile != null ? findReferenceAt(psiFile, offset) : null;
  }

  public Lexer createLexer(final Language language) {
    if(language != getBaseLanguage() && language != Language.ANY) return null;
    final ParserDefinition parserDefinition = language.getParserDefinition();
    if(parserDefinition == null) return ((PsiFileImpl)getPsi(getBaseLanguage())).createLexer();
    return parserDefinition.createLexer(getManager().getProject());
  }

  protected PsiReference findReferenceAt(final PsiFile psiFile, final int offset) {
    int offsetInElement = offset;
    PsiElement child = psiFile.getFirstChild();
    while(child != null){
      final int length = child.getTextLength();
      if(length <= offsetInElement) {
        offsetInElement -= length;
        child = child.getNextSibling();
        continue;
      }
      return child.findReferenceAt(offsetInElement);
    }
    return null;
  }

  public PsiElement findElementAt(final int offset) {
    final PsiFileImpl psiFile = (PsiFileImpl)getPsi(getBaseLanguage());
    return findElementAt(psiFile, offset);
  }

  protected PsiElement findElementAt(final PsiElement psiFile, final int offset) {
    int offsetInElement = offset;
    PsiElement child = psiFile.getFirstChild();
    while(child != null){
      final int length = child.getTextLength();
      if(length <= offsetInElement) {
        offsetInElement -= length;
        child = child.getNextSibling();
        continue;
      }
      return child.findElementAt(offsetInElement);
    }
    return null;
  }

  public synchronized void forceCachedPsi(final PsiFile psiCodeFragment) {
    myPsiFile = psiCodeFragment;
    ((PsiManagerImpl)myManager).getFileManager().setViewProvider(getVirtualFile(), this);
  }

  private Content getContent() {
    return myContent;
  }

  private void setContent(final Content content) {
    myContent = content;
  }

  private static interface Content{
    CharSequence getText();
    long getModificationStamp();
  }

  private class VirtualFileContent implements Content{
    public CharSequence getText() {
      final Document document = getDocument();
      if (document == null) {
        return LoadTextUtil.loadText(getVirtualFile());
      }
      else {
        return document.getCharsSequence();
      }
    }

    public long getModificationStamp() {
      return getVirtualFile().getModificationStamp();
    }
  }

  private class DocumentContent implements Content{
    public CharSequence getText() {
      return getDocument().getCharsSequence();
    }

    public long getModificationStamp() {
      return getDocument().getModificationStamp();
    }
  }

  private class PsiFileContent implements Content{
    private PsiFileImpl myFile;
    private CharSequence myContent = null;
    private long myModificationStamp;

    public PsiFileContent(final PsiFileImpl file, final long modificationStamp) {
      myFile = file;
      myModificationStamp = modificationStamp;
    }

    public CharSequence getText() {
      if(!myFile.isContentsLoaded()){
        unsetPsiContent();
        return getContents();
      }
      if(myContent != null) return myContent;
      return myContent = myFile.calcTreeElement().getText();
    }

    public long getModificationStamp() {
      if(!myFile.isContentsLoaded()){
        unsetPsiContent();
        return SingleRootFileViewProvider.this.getModificationStamp();
      }
      return myModificationStamp;
    }
  }
}