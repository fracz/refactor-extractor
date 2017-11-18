/*
 * @author max
 */
package com.intellij.psi;

import com.intellij.lang.Language;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.SharedPsiElementImplUtil;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.impl.source.jsp.jspJava.OuterLanguageElement;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.ReflectionCache;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiplePsiFilesPerDocumentFileViewProvider extends SingleRootFileViewProvider {
  private final Map<Language, PsiFile> myRoots = new HashMap<Language, PsiFile>();
  private MultiplePsiFilesPerDocumentFileViewProvider myOriginal = null;

  public MultiplePsiFilesPerDocumentFileViewProvider(PsiManager manager, VirtualFile file) {
    super(manager, file);
  }

  public MultiplePsiFilesPerDocumentFileViewProvider(PsiManager manager, VirtualFile virtualFile, boolean physical) {
    super(manager, virtualFile, physical);
  }


  public List<PsiFile> getAllFiles() {
    final ArrayList<PsiFile> roots = new ArrayList<PsiFile>(myRoots.values());
    final PsiFile base = myRoots.get(getBaseLanguage());
    if (roots.size() > 0 && roots.get(0) != base) {
      roots.remove(base);
      roots.add(0, base);
    }
    return roots;
  }

  protected PsiFile getPsiInner(final Language target) {
    synchronized (PsiLock.LOCK) {
      PsiFile file = myRoots.get(target);
      if (file == null) {
        file = createFile(target);
        if (file == null) return null;
        /*
        if (myOriginal != null) {
          ((PsiFileImpl)file).myOriginalFile = myOriginal.getPsi(target);
        }
        */
        myRoots.put(target, file);
      }
      return file;
    }
  }


  public PsiFile getCachedPsi(Language target) {
    synchronized (PsiLock.LOCK) {
      return myRoots.get(target);
    }
  }

  public FileElement[] getKnownTreeRoots() {
    List<FileElement> files = new ArrayList<FileElement>(myRoots.size());
    for (PsiFile file : myRoots.values()) {
      final FileElement treeElement = ((PsiFileImpl)file).getTreeElement();
      if (treeElement != null) {
        files.add(treeElement);
      }
    }

    return files.toArray(new FileElement[files.size()]);
  }

  public void checkAllTreesEqual() {
    //TODO
  }

  public FileViewProvider clone() {
    final MultiplePsiFilesPerDocumentFileViewProvider copy = (MultiplePsiFilesPerDocumentFileViewProvider)cloneInner(new LightVirtualFile(getVirtualFile(), getContents(), getModificationStamp()));
    if (myOriginal == null) {
      copy.myOriginal = this;
    }
    else {
      copy.myOriginal = myOriginal;
    }

    return copy;
  }

  protected FileViewProvider cloneInner(VirtualFile fileCopy) {
    return new MultiplePsiFilesPerDocumentFileViewProvider(getManager(), fileCopy, false);
  }

  @Nullable
  public PsiElement findElementAt(int offset, Class<? extends Language> lang) {
    final PsiFile mainRoot = getPsi(getBaseLanguage());
    PsiElement ret = null;
    for (final Language language : getRelevantLanguages()) {
      if (!ReflectionCache.isAssignable(lang, language.getClass())) continue;
      if (lang.equals(Language.class) && !getPrimaryLanguages().contains(language)) continue;

      final PsiFile psiRoot = getPsi(language);
      final PsiElement psiElement = findElementAt(psiRoot, offset);
      if (psiElement == null || psiElement instanceof OuterLanguageElement) continue;
      if (ret == null || psiRoot != mainRoot) {
        ret = psiElement;
      }
    }
    return ret;
  }

  @Nullable
  public PsiElement findElementAt(int offset) {
    return findElementAt(offset, Language.class);
  }

  @Nullable
  public PsiReference findReferenceAt(int offset) {
    TextRange minRange = new TextRange(0, getContents().length());
    PsiReference ret = null;
    for (final Language language : getPrimaryLanguages()) {
      final PsiElement psiRoot = getPsi(language);
      final PsiReference reference = SharedPsiElementImplUtil.findReferenceAt(psiRoot, offset);
      if (reference == null) continue;
      final TextRange textRange = reference.getRangeInElement().shiftRight(reference.getElement().getTextRange().getStartOffset());
      if (minRange.contains(textRange)) {
        minRange = textRange;
        ret = reference;
      }
    }
    return ret;
  }

  public void loadAllRoots() {
    for (Language lang : getRelevantLanguages()) {
      getPsi(lang);
    }
  }
}