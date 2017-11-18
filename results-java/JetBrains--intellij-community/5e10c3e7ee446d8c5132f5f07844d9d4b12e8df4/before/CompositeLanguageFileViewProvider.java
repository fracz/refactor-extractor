package com.intellij.psi;

import com.intellij.lang.*;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.psi.impl.SharedPsiElementImplUtil;
import com.intellij.psi.impl.source.LightPsiFileImpl;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.impl.source.codeStyle.TooComplexPSIModificationException;
import com.intellij.psi.impl.source.jsp.CompositeLanguageParsingUtil;
import com.intellij.psi.impl.source.jsp.JspImplUtil;
import com.intellij.psi.impl.source.jsp.jspJava.JspWhileStatement;
import com.intellij.psi.impl.source.jsp.jspJava.OuterLanguageElement;
import com.intellij.psi.impl.source.parsing.ChameleonTransforming;
import com.intellij.psi.impl.source.parsing.ParseUtil;
import com.intellij.psi.impl.source.tree.*;
import com.intellij.psi.jsp.JspElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlText;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ReflectionCache;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;

import java.util.*;

public class CompositeLanguageFileViewProvider extends SingleRootFileViewProvider {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.CompositeLanguageFileViewProvider");
  public static final Key<Object> UPDATE_IN_PROGRESS = new Key<Object>("UPDATE_IN_PROGRESS");
  public static final Key<Object> DO_NOT_UPDATE_AUX_TREES = new Key<Object>("DO_NOT_UPDATE_AUX_TREES");
  public static final Key<Integer> OUTER_LANGUAGE_MERGE_POINT = new Key<Integer>("OUTER_LANGUAGE_MERGE_POINT");
  private final Map<Language, PsiFile> myRoots = new HashMap<Language, PsiFile>();
  private Set<Language> myRelevantLanguages;

  public Set<Language> getRelevantLanguages() {
    if (myRelevantLanguages != null) return myRelevantLanguages;
    Set<Language> relevantLanguages = new HashSet<Language>();
    final Language baseLanguage = getBaseLanguage();
    relevantLanguages.add(baseLanguage);
    relevantLanguages.addAll(myRoots.keySet());
    return myRelevantLanguages = new LinkedHashSet<Language>(relevantLanguages);
  }

  public void contentsSynchronized() {
    super.contentsSynchronized();
    myRelevantLanguages = null;
  }

  private final Map<PsiFile, Set<OuterLanguageElement>> myOuterLanguageElements =
    new HashMap<PsiFile, Set<OuterLanguageElement>>();
  private Set<PsiFile> myRootsInUpdate = new HashSet<PsiFile>(4);

  public CompositeLanguageFileViewProvider(final PsiManager manager, final VirtualFile file) {
    super(manager, file);
  }

  public CompositeLanguageFileViewProvider(final PsiManager manager, final VirtualFile virtualFile, final boolean physical) {
    super(manager, virtualFile, physical);
  }

  public CompositeLanguageFileViewProvider clone() {
    LightVirtualFile fileCopy = new LightVirtualFile(getVirtualFile(), getContents(), getModificationStamp());
    final CompositeLanguageFileViewProvider viewProvider = cloneInner(fileCopy);
    final PsiFileImpl psiFile = (PsiFileImpl)viewProvider.getPsi(getBaseLanguage());

    // copying main tree
    final FileElement treeClone = (FileElement)psiFile.calcTreeElement().clone(); // base language tree clone
    psiFile.setTreeElementPointer(treeClone); // should not use setTreeElement here because cloned file still have VirtualFile (SCR17963)
    treeClone.setPsiElement(psiFile);

    final XmlText[] xmlTexts = JspImplUtil.gatherTexts((XmlFile)psiFile);
    for (Map.Entry<Language, PsiFile> entry : myRoots.entrySet()) {
      final PsiFile root = entry.getValue();
      if (root != psiFile) {
        if (root instanceof PsiFileImpl) {
          final PsiFileImpl copy = (PsiFileImpl)viewProvider.getPsi(entry.getKey());
          if (copy == null) continue; // Unreleivant language due to partial parsing.
          JspImplUtil.copyRoot((PsiFileImpl)root, xmlTexts, copy);
        }
        else {
          if (root instanceof LightPsiFileImpl) {
            final LightPsiFileImpl lightFile = (LightPsiFileImpl)root;
            final LightPsiFileImpl clone = lightFile.copyLight(viewProvider);
            clone.setOriginalFile(root);
            viewProvider.myRoots.put(entry.getKey(), clone);
          }
        }
      }
    }
    return viewProvider;
  }

  protected CompositeLanguageFileViewProvider cloneInner(VirtualFile copy) {
    return new CompositeLanguageFileViewProvider(getManager(), copy, false);
  }

  @Nullable
  protected PsiFile getPsiInner(Language target) {
    PsiFile file = super.getPsiInner(target);
    if (file != null) return file;
    file = myRoots.get(target);
    if (file == null) {
      synchronized (PsiLock.LOCK) {
        file = createFile(target);
        myRoots.put(target, file);
      }
    }
    return file;
  }

  public synchronized PsiFile getCachedPsi(Language target) {
    if (target == getBaseLanguage()) return super.getCachedPsi(target);
    return myRoots.get(target);
  }

  public void checkAllTreesEqual() {
    final String psiText = getPsi(getBaseLanguage()).getText();
    for (Map.Entry<Language, PsiFile> entry : myRoots.entrySet()) {
      final PsiFile psiFile = entry.getValue();
      LOG.assertTrue(psiFile.getTextLength() == psiText.length(), entry.getKey().getID() + " tree text differs from base!");
      LOG.assertTrue(psiFile.getText().equals(psiText), entry.getKey().getID() + " tree text differs from base!");
    }
  }

  public void registerOuterLanguageElement(OuterLanguageElement element, PsiFile root) {
    if (myRoots.get(root.getLanguage()) != root) {
      return; // Not mine. Probably comes from air elements parsing.
    }

    Set<OuterLanguageElement> outerLanguageElements = myOuterLanguageElements.get(root);
    if (outerLanguageElements == null) {
      outerLanguageElements = new TreeSet<OuterLanguageElement>(new Comparator<OuterLanguageElement>() {
        public int compare(final OuterLanguageElement languageElement, final OuterLanguageElement languageElement2) {
          if (languageElement.equals(languageElement2)) return 0;
          final int result = languageElement.getTextRange().getStartOffset() - languageElement2.getTextRange().getStartOffset();
          if (result != 0) return result;
          return languageElement.getTextRange().getEndOffset() - languageElement2.getTextRange().getEndOffset();
        }
      });
      myOuterLanguageElements.put(root, outerLanguageElements);
    }
    outerLanguageElements.add(element);
  }

  public void reparseRoots(final Set<Language> rootsToReparse) {
    for (final Language lang : rootsToReparse) {
      final PsiFile cachedRoot = myRoots.get(lang);
      if (cachedRoot != null) {
        if (myRootsInUpdate.contains(cachedRoot)) continue;
        try {
          myRootsInUpdate.add(cachedRoot);
          reparseRoot(lang, cachedRoot);
        }
        finally {
          myRootsInUpdate.remove(cachedRoot);
        }
      }
    }
  }


  public FileElement[] getKnownTreeRoots() {
    final List<FileElement> knownRoots = new ArrayList<FileElement>();
    knownRoots.addAll(Arrays.asList(super.getKnownTreeRoots()));
    for (PsiFile psiFile : myRoots.values()) {
      if (psiFile == null || !(psiFile instanceof PsiFileImpl)) continue;
      final FileElement fileElement = ((PsiFileImpl)psiFile).getTreeElement();
      if (fileElement == null) continue;
      knownRoots.add(fileElement);
    }
    return knownRoots.toArray(new FileElement[knownRoots.size()]);
  }

  private void reparseRoot(final Language lang, final PsiFile cachedRoot) {
    LOG.debug("JspxFile: reparseRoot " + getVirtualFile().getName());
    final ASTNode oldFileTree = cachedRoot.getNode();
    if (oldFileTree == null || oldFileTree.getFirstChildNode()instanceof ChameleonElement) {
      if (cachedRoot instanceof PsiFileImpl) ((PsiFileImpl)cachedRoot).setTreeElementPointer(null);
      cachedRoot.subtreeChanged();
      return;
    }
    final PsiFile fileForNewText = createFile(lang);
    assert fileForNewText != null;
    final ASTNode newFileTree = fileForNewText.getNode();
    if (LOG.isDebugEnabled()) {
      LOG.debug("Old Tree:\n" + DebugUtil.treeToString(oldFileTree, false, true));
      LOG.debug("New Tree:\n" + DebugUtil.treeToString(newFileTree, false, true));
    }
    ChameleonTransforming.transformChildren(newFileTree, true);
    ChameleonTransforming.transformChildren(oldFileTree, true);
    CompositeLanguageParsingUtil.mergeTreeElements((TreeElement)newFileTree.getFirstChildNode(),
                                                   (TreeElement)oldFileTree.getFirstChildNode(),
                                                   (CompositeElement)oldFileTree,
                                                   this
                                                   );
    checkConsistensy(cachedRoot);
  }

  public void updateOuterLanguageElements(final Set<Language> languagesToSkip) {
    for (Map.Entry<Language, PsiFile> entry : myRoots.entrySet()) {
      final PsiFile psiFile = entry.getValue();
      final Language updatedLanguage = entry.getKey();
      if (languagesToSkip.contains(updatedLanguage) || isNotRelevantLanguageDueToContentChange(updatedLanguage)) continue;
      Set<OuterLanguageElement> outerSet = myOuterLanguageElements.get(psiFile);
      if (outerSet == null) // not parsed yet
      {
        continue;
      }
      try {
        myRootsInUpdate.add(psiFile);

        Set<OuterLanguageElement> languageElements = outerSet;
        if (languageElements == null) {
          languageElements = recalcOuterElements(psiFile);
        }

        for (final OuterLanguageElement outerElement : languageElements) {
          final FileElement file = TreeUtil.getFileElement(outerElement);

          if (file == null) {
            languageElements.clear();
            languageElements = recalcOuterElements(psiFile);
            break;
          }
        }

        Iterator<OuterLanguageElement> i = languageElements.iterator();
        while (i.hasNext()) {
          final OuterLanguageElement outerElement = i.next();
          final FileElement file = TreeUtil.getFileElement(outerElement);

          if (file == null || file.getPsi() != psiFile) {
            i.remove();
          }
        }

        XmlText prevText = null;

        i = languageElements.iterator();

        int outerCount = 0;
        while (i.hasNext()) {
          final OuterLanguageElement outerElement = i.next();
          XmlText nextText = outerElement.getFollowingText();

          final int start =
            prevText != null ? prevText.getTextRange().getEndOffset() : outerCount != 0 ? outerElement.getTextRange().getStartOffset() : 0;

          if (nextText != null && !nextText.isValid()) {
            final PsiElement nextSibiling = outerElement.getNextSibling();
            assert nextSibiling == null || nextSibiling instanceof OuterLanguageElement;
            nextText = nextSibiling != null ? ((OuterLanguageElement)nextSibiling).getFollowingText() : null;

            final int end = nextText != null ? nextText.getTextRange().getStartOffset() : getContents().length();
            assert start <= end;
            final TextRange textRange = new TextRange(start, end);
            outerElement.setRange(textRange);

            if (nextSibiling != null) {
              outerElement.putUserData(OUTER_LANGUAGE_MERGE_POINT, end - nextSibiling.getTextRange().getLength() - start);
              TreeUtil.remove((OuterLanguageElement)nextSibiling);
              final OuterLanguageElement next = i.next();
              assert next == nextSibiling;
              i.remove();
            }
          }
          else {

            int end;
            if (nextText != null) {
              end = nextText.getTextRange().getStartOffset();
              end += nextText.displayToPhysical(0);
            }
            else {
              end = i.hasNext() ? outerElement.getTextRange().getEndOffset() : getContents().length();
            }

            assert start <= end;
            final TextRange textRange = new TextRange(start, end);

            if (!textRange.equals(outerElement.getTextRange())) {
              outerElement.setRange(textRange);
            }
          }
          prevText = nextText;
          ++outerCount;
        }

      }
      finally {
        myRootsInUpdate.remove(psiFile);
      }

      checkConsistensy(psiFile);
    }
  }

  protected boolean isNotRelevantLanguageDueToContentChange(final Language updatedLanguage) {
    return false;
  }

  private Set<OuterLanguageElement> recalcOuterElements(final PsiFile psiFile) {
    psiFile.accept(new PsiElementVisitor() {
      public void visitReferenceExpression(PsiReferenceExpression expression) {
      }

      public void visitElement(PsiElement element) {
        if (element instanceof OuterLanguageElement) {
          final OuterLanguageElement outerLanguageElement = (OuterLanguageElement)element;

          final FileElement file = TreeUtil.getFileElement(outerLanguageElement);
          if (file.getPsi() == psiFile) {
            registerOuterLanguageElement(outerLanguageElement, psiFile);
          }
        }
        else {
          element.acceptChildren(this);
        }
      }
    });

    return myOuterLanguageElements.get(psiFile);
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

  private void checkConsistensy(final PsiFile oldFile) {
    ASTNode oldNode = oldFile.getNode();
    if (oldNode.getTextLength() != getContents().length() ||
       !oldNode.getText().equals(getContents().toString())) {
      @NonNls String message = "Check consistency failed for: " + oldFile;
      message += "\n     oldFile.getNode().getTextLength() = " + oldNode.getTextLength();
      message += "\n     getContents().length() = " + getContents().length();
      message += "\n     language = " + oldFile.getLanguage();

      if (ApplicationManagerEx.getApplicationEx().isInternal()) {
        message += "\n     oldFileText:\n" + oldNode.getText();
        message += "\n     contentsText:\n" + getContents().toString();
        message += "\n     jspText:\n" + getPsi(getBaseLanguage()).getNode().getText();
      }
      LOG.assertTrue(false, message);
      assert false;
    }
  }

  public LanguageExtension[] getLanguageExtensions() {
    return new LanguageExtension[0];
  }

  protected void removeFile(Language lang) {
    myRoots.remove(lang);
    myOuterLanguageElements.remove(lang);
  }

  @Nullable
  protected PsiFile createFile(Language lang) {
    final PsiFile psiFile = super.createFile(lang);
    if (psiFile != null) return psiFile;
    if (getRelevantLanguages().contains(lang)) {
      final ParserDefinition parserDefinition = lang.getParserDefinition();
      assert parserDefinition != null;
      return parserDefinition.createFile(this);
    }
    return null;
  }

  public void rootChanged(PsiFile psiFile) {
    if (myRootsInUpdate.contains(psiFile)) return;
    if (psiFile.getLanguage() == getBaseLanguage()) {
      super.rootChanged(psiFile);
      // rest of sync mechanism is now handled by JspxAspect
    }
    else if (!myRootsInUpdate.contains(getPsi(getBaseLanguage()))) {
      try {
        doHolderToXmlChanges(psiFile);
      }
      catch (Throwable e) {
        LOG.error(e);
        emergencyReparse();
      }
    }
  }

  private volatile boolean myInEmergencyReparse = false;
  public void emergencyReparse() {
    if (myInEmergencyReparse) return;
    myInEmergencyReparse = true;
    try {
      myRootsInUpdate.addAll(myRoots.values());
      for (final Language lang : getRelevantLanguages()) {
        final PsiFile cachedRoot = myRoots.get(lang);
        if (cachedRoot != null) {
          reparseRoot(lang, cachedRoot);
        }
      }
    }
    catch (Throwable e) {
      throw new TooComplexPSIModificationException();
      // We've already logged all important stuff.
    }
    finally {
      myRootsInUpdate.clear();
      myInEmergencyReparse = false;
    }
  }

  private void doHolderToXmlChanges(final PsiFile psiFile) {
    putUserData(UPDATE_IN_PROGRESS, Boolean.TRUE);

    boolean buffersDiffer = false;

    final Language language = getBaseLanguage();
    final List<Pair<OuterLanguageElement, Pair<StringBuffer, StringBuffer>>> javaFragments =
      new ArrayList<Pair<OuterLanguageElement, Pair<StringBuffer, StringBuffer>>>();
    try {
      StringBuffer currentBuffer = null;
      StringBuffer currentDecodedBuffer = null;
      LeafElement element = TreeUtil.findFirstLeaf(psiFile.getNode());
      if (element == null) return;
      do {
        if (element instanceof OuterLanguageElement) {
          currentDecodedBuffer = new StringBuffer();
          currentBuffer = new StringBuffer();
          javaFragments.add(Pair.create((OuterLanguageElement)element, Pair.create(currentBuffer, currentDecodedBuffer)));
        }
        else {
          String text = element.getText();

          if (element instanceof PsiWhiteSpace && text.endsWith("]]>")) {
            text = text.substring(0, text.length() - "]]>".length());
          }

          final String decoded = language != StdLanguages.JSP ? XmlUtil.decode(text) : text;
          assert currentDecodedBuffer != null;
          currentDecodedBuffer.append(decoded);
          currentBuffer.append(text);
        }

        if (!buffersDiffer && !Comparing.equal(currentBuffer, currentDecodedBuffer)) {
          buffersDiffer = true;
        }
      }
      while ((element = ParseUtil.nextLeaf(element, null)) != null);

      if (!buffersDiffer) {
        myRootsInUpdate.add(psiFile);
      }

      for (final Pair<OuterLanguageElement, Pair<StringBuffer, StringBuffer>> pair : javaFragments) {
        final XmlText followingText = pair.getFirst().getFollowingText();
        final String newText = pair.getSecond().getSecond().toString();
        if (followingText != null && followingText.isValid() && !followingText.getValue().equals(newText)) {
          followingText.setValue(newText);
        }
      }
    }
    catch (IncorrectOperationException e) {
      LOG.error(e);
    }
    finally {
      updateOuterLanguageElementsIn(psiFile);
      if (!buffersDiffer) {
        myRootsInUpdate.remove(psiFile);
      }
      putUserData(UPDATE_IN_PROGRESS, null);
    }
  }

  private void updateOuterLanguageElementsIn(final PsiFile psiFile) {
    final HashSet<Language> set = new HashSet<Language>(getRelevantLanguages());
    set.remove(psiFile.getLanguage());
    updateOuterLanguageElements(set);
  }

  public void normalizeOuterLanguageElements(PsiFile root) {
    if (!myRootsInUpdate.contains(root)) {
      try {
        myRootsInUpdate.add(root);
        normalizeOuterLanguageElementsInner(root);
      }
      finally {
        myRootsInUpdate.remove(root);
      }
    }
    else {
      normalizeOuterLanguageElementsInner(root);
    }
  }

  private void normalizeOuterLanguageElementsInner(final PsiFile lang) {
    final Set<OuterLanguageElement> outerElements = myOuterLanguageElements.get(lang);
    if (outerElements == null) return;
    final Iterator<OuterLanguageElement> iterator = outerElements.iterator();
    final List<OuterLanguageElement> toRemove = new ArrayList<OuterLanguageElement>();

    OuterLanguageElement prev = null;
    while (iterator.hasNext()) {
      final OuterLanguageElement outer = iterator.next();

      if (outer == null) {
        iterator.remove();
        continue;
      }

      if (prev != null && prev.getFollowingText() == null && outer.getTextRange().getStartOffset() == prev.getTextRange().getEndOffset()) {
        final CompositeElement prevParent = prev.getTreeParent();
        final JspWhileStatement outerWhile = PsiTreeUtil.getParentOfType(outer, JspWhileStatement.class);

        final JspWhileStatement prevWhile = PsiTreeUtil.getParentOfType(prev, JspWhileStatement.class);

        if (prevParent != null && prevParent.getElementType() == JspElementType.JSP_TEMPLATE_EXPRESSION || outerWhile != null || prevWhile != null) {
          if (prevWhile == outerWhile) {
            toRemove.add(prev);
            prev = mergeOuterLanguageElements(outer, prev);
          }
          else {
            prev = outer;
          }
        }
        else {
          toRemove.add(outer);
          prev = mergeOuterLanguageElements(prev, outer);
        }
      }
      else {
        prev = outer;
      }
    }

    outerElements.removeAll(toRemove);
  }

  private static OuterLanguageElement mergeOuterLanguageElements(final OuterLanguageElement prev, final OuterLanguageElement outer) {
    final TextRange textRange = new TextRange(Math.min(prev.getTextRange().getStartOffset(), outer.getTextRange().getStartOffset()),
                                              Math.max(prev.getTextRange().getEndOffset(), outer.getTextRange().getEndOffset()));
    prev.setRange(textRange);
    if (prev.getFollowingText() == null) prev.setFollowingText(outer.getFollowingText());
    final CompositeElement parent = prev.getTreeParent();
    if (parent != null) parent.subtreeChanged();
    final CompositeElement removedParent = outer.getTreeParent();
    TreeUtil.remove(outer);
    if (removedParent != null) removedParent.subtreeChanged();
    return prev;
  }

  public Set<PsiFile> getRootsInUpdate() {
    return myRootsInUpdate;
  }
}