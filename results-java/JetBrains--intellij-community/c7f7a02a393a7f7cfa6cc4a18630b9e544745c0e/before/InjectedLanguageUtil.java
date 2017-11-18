package com.intellij.psi.impl.source.tree.injected;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageDialect;
import com.intellij.lang.ParserDefinition;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.impl.injected.DocumentRange;
import com.intellij.openapi.editor.impl.injected.EditorDelegate;
import com.intellij.openapi.editor.impl.injected.VirtualFileDelegate;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiDocumentManagerImpl;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.impl.source.resolve.ResolveUtil;
import com.intellij.psi.impl.source.tree.*;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.SmartList;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author cdr
 */
public class InjectedLanguageUtil {
  private static final Logger LOG = Logger.getInstance("com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtill");
  private static final Key<CachedValue<List<Pair<PsiElement, TextRange>>>> INJECTED_PSI = Key.create("injectedPsi");

  @Nullable
  public static <T extends PsiLanguageInjectionHost> List<Pair<PsiElement, TextRange>> getInjectedPsiFiles(@NotNull T host, @Nullable LiteralTextEscaper<T> textEscaper) {
    if (!host.isPhysical()) {
      CachedValueProvider.Result<List<Pair<PsiElement, TextRange>>> result = new InjectedPsiProvider<T>(host, textEscaper).compute();
      return result == null ? null : result.getValue();
    }

    CachedValue<List<Pair<PsiElement, TextRange>>> cachedPsi = host.getUserData(INJECTED_PSI);
    if (cachedPsi == null) {
      InjectedPsiProvider<T> provider = new InjectedPsiProvider<T>(host, textEscaper);
      CachedValueProvider.Result<List<Pair<PsiElement, TextRange>>> result = provider.compute();
      if (result == null) return null;
      cachedPsi = host.getManager().getCachedValuesManager().createCachedValue(provider, false);
      ((UserDataHolderEx)host).putUserDataIfAbsent(INJECTED_PSI, cachedPsi);
      return result.getValue();
    }
    return cachedPsi.getValue();
  }

  private static <T extends PsiLanguageInjectionHost> PsiElement parseInjectedPsiFile(@NotNull final T host,
                                                                                      @NotNull final TextRange rangeInsideHost,
                                                                                      @NotNull final Language language,
                                                                                      @NotNull final VirtualFile hostVirtualFile,
                                                                                      @NotNull final TextRange hostRange,
                                                                                      @NotNull final DocumentEx hostDocument,
                                                                                      LiteralTextEscaper<T> textEscaper,
                                                                                      @NotNull String prefix,
                                                                                      @NotNull String suffix) {
    TextRange documentWindow = hostRange.cutOut(rangeInsideHost);
    DocumentRange documentRange = new DocumentRange(hostDocument, documentWindow,prefix,suffix);
    VirtualFileDelegate virtualFile = new VirtualFileDelegate(hostVirtualFile, documentRange, language, documentRange.getText());

    final ParserDefinition parserDefinition = language.getParserDefinition();
    if (parserDefinition == null) return null;
    PsiFile hostFile = host.getContainingFile();
    if (hostFile == null) return null;
    PsiManager psiManager = host.getManager();
    final Project project = psiManager.getProject();

    final String hostText = host.getText();
    StringBuilder outChars = new StringBuilder(hostText.length());
    outChars.append(prefix);
    if (textEscaper == null) {
      outChars.append(hostText, rangeInsideHost.getStartOffset(), rangeInsideHost.getEndOffset());
    }
    else {
      boolean result = textEscaper.decode(host, rangeInsideHost, outChars);
      if (!result) return null;
    }
    outChars.append(suffix);
    virtualFile.setContent(null, outChars, false);
    DocumentImpl decodedDocument = new DocumentImpl(outChars);
    FileDocumentManagerImpl.registerDocument(decodedDocument, virtualFile);

    SingleRootFileViewProvider viewProvider = new MyFileViewProvider(project, virtualFile);
    PsiFile psiFile = parserDefinition.createFile(viewProvider);
    assert psiFile.getViewProvider() instanceof MyFileViewProvider : psiFile.getViewProvider();

    SmartPsiElementPointer<T> pointer = createHostSmartPointer(host);
    psiFile.putUserData(ResolveUtil.INJECTED_IN_ELEMENT, pointer);
    psiFile.putUserData(PsiManagerImpl.LANGUAGE_DIALECT,language instanceof LanguageDialect ? (LanguageDialect)language:null);

    final ASTNode parsedNode = psiFile.getNode();
    if (!(parsedNode instanceof FileElement)) return null;
    String sourceRawText = prefix + hostText.substring(rangeInsideHost.getStartOffset(), rangeInsideHost.getEndOffset()) + suffix;
    if (textEscaper != null) {
      patchLeafs(parsedNode, textEscaper, rangeInsideHost, hostText, prefix.length(), suffix.length());
    }
    String parsedText = parsedNode.getText();
    LOG.assertTrue(parsedText.equals(sourceRawText));

    parsedNode.putUserData(TreeElement.MANAGER_KEY, psiManager);
    virtualFile.setContent(null, documentRange.getText(), false);
    FileDocumentManagerImpl.registerDocument(documentRange, virtualFile);
    synchronized (PsiLock.LOCK) {
      psiFile = registerDocumentRange(documentRange, psiFile);
    }

    ((MyFileViewProvider)psiFile.getViewProvider()).setVirtualFile(virtualFile);
    ((SingleRootFileViewProvider)psiFile.getViewProvider()).forceCachedPsi(psiFile);

    PsiDocumentManagerImpl.checkConsistency(psiFile, documentRange);
    PsiDocumentManagerImpl.checkConsistency(hostFile, documentRange.getDelegate());

    return psiFile;
  }

  private static class MyFileViewProvider extends SingleRootFileViewProvider {
    public MyFileViewProvider(@NotNull Project project, @NotNull VirtualFileDelegate virtualFile) {
      super(PsiManager.getInstance(project), virtualFile);
    }

    public void rootChanged(PsiFile psiFile) {
      super.rootChanged(psiFile);
      PsiDocumentManager documentManager = PsiDocumentManager.getInstance(getManager().getProject());
      DocumentRange documentRange = (DocumentRange)documentManager.getDocument(psiFile);
      String text = psiFile.getText();
      text = StringUtil.trimStart(text, documentRange.getPrefix());
      text = StringUtil.trimEnd(text, documentRange.getSuffix());
      PsiLanguageInjectionHost injectionHost = (PsiLanguageInjectionHost)psiFile.getContext();
      TextRange hostTextRange = injectionHost.getTextRange();
      RangeMarker documentWindow = documentRange.getTextRange();
      TextRange rangeInsideHost = new TextRange(documentWindow.getStartOffset(), documentWindow.getEndOffset()).shiftRight(-hostTextRange.getStartOffset());
      String newHostText = StringUtil.replaceSubstring(injectionHost.getText(), rangeInsideHost, text);
      if (!newHostText.equals(injectionHost.getText())) {
        injectionHost.fixText(newHostText);
      }
    }

    public FileViewProvider clone() {
      DocumentRange oldDocumentRange = ((VirtualFileDelegate)getVirtualFile()).getDocumentRange();
      RangeMarker documentWindow = oldDocumentRange.getTextRange();
      DocumentEx delegate = oldDocumentRange.getDelegate();
      PsiDocumentManager documentManager = PsiDocumentManager.getInstance(getManager().getProject());
      PsiFile hostFile = documentManager.getPsiFile(delegate);
      PsiFile hostPsiFileCopy = (PsiFile)hostFile.copy();

      TextRange oldTextRange = new TextRange(documentWindow.getStartOffset(), documentWindow.getEndOffset());
      PsiLanguageInjectionHost newHost = findInjectionHost(hostPsiFileCopy.findElementAt(oldDocumentRange.getTextRange().getStartOffset()));
      assert newHost != null;
      List<Pair<PsiElement, TextRange>> newInjected = newHost.getInjectedPsi();
      if (newInjected == null) return null;
      for (Pair<PsiElement, TextRange> pair : newInjected) {
        PsiElement psi = pair.getFirst();
        TextRange rangeInsideHost = pair.getSecond();
        TextRange injectedRange = newHost.getTextRange().cutOut(rangeInsideHost);
        if (injectedRange.equals(oldTextRange)) {
          PsiFile newFile = (PsiFile)psi;
          return newFile.getViewProvider();
        }
      }

      return null;
    }
  }

  private static <T extends PsiLanguageInjectionHost> void patchLeafs(final ASTNode parsedNode,
                                                                      final LiteralTextEscaper<T> literalTextEscaper,
                                                                      final TextRange rangeInsideHost, final String hostText,
                                                                      final int prefixLength, final int suffixLength) {
    final TextRange contentsRange = new TextRange(prefixLength, parsedNode.getTextLength() - suffixLength);

    final Map<LeafElement, String> newTexts = new THashMap<LeafElement, String>();
    ((TreeElement)parsedNode).acceptTree(new RecursiveTreeElementVisitor(){
      int currentSourceOffset = rangeInsideHost.getStartOffset();
      protected boolean visitNode(TreeElement element) {
        return true;
      }

      public void visitLeaf(LeafElement leaf) {
        TextRange range = leaf.getTextRange();
        if (prefixLength > range.getStartOffset() && prefixLength < range.getEndOffset()) {
          LOG.error("Prefix must not contain text that will be glued with the element body after parsing. " +
                    "However, parsed element of "+leaf.getClass()+" contains "+(prefixLength-range.getStartOffset()) + " characters from the prefix. " +
                    "Parsed text is '"+leaf.getText()+"'");
        }
        if (range.getStartOffset() < contentsRange.getEndOffset() && contentsRange.getEndOffset() < range.getEndOffset()) {
          LOG.error("Suffix must not contain text that will be glued with the element body after parsing. " +
                    "However, parsed element of "+leaf.getClass()+" contains "+(range.getEndOffset()-contentsRange.getEndOffset()) + " characters from the suffix. " +
                    "Parsed text is '"+leaf.getText()+"'");
        }
        if (!contentsRange.contains(range)) return;
        int offsetInSource = currentSourceOffset;
        int endOffsetInSource = literalTextEscaper.getOffsetInHost(range.getEndOffset()-prefixLength, rangeInsideHost);
        String hostSubText = hostText.substring(offsetInSource, endOffsetInSource);
        String leafText = leaf.getText();
        if (!Comparing.strEqual(leafText, hostSubText)) {
          newTexts.put(leaf, hostSubText);
        }
        currentSourceOffset += endOffsetInSource-offsetInSource;
      }
    });

    for (LeafElement leaf : newTexts.keySet()) {
      String newText = newTexts.get(leaf);
      leaf.setText(newText);
    }
    ((TreeElement)parsedNode).acceptTree(new RecursiveTreeElementVisitor(){
      protected boolean visitNode(TreeElement element) {
        element.clearCaches();
        return true;
      }
    });
  }

  public static Editor getEditorForInjectedLanguage(final Editor editor, PsiFile file) {
    if (editor == null || file == null || editor instanceof EditorDelegate) return editor;

    int offset = editor.getCaretModel().getOffset();
    return getEditorForInjectedLanguage(editor, file, offset);
  }

  public static Editor getEditorForInjectedLanguage(final Editor editor, final PsiFile file, final int offset) {
    if (editor == null || file == null || editor instanceof EditorDelegate) return editor;
    PsiDocumentManager.getInstance(file.getProject()).commitAllDocuments();
    PsiLanguageInjectionHost injectionHost = findInjectionHost(file.findElementAt(offset));
    if (injectionHost == null && offset != 0) {
      injectionHost = findInjectionHost(file.findElementAt(offset-1));
    }
    List<Pair<PsiElement, TextRange>> injectedPsi = injectionHost == null ? null : injectionHost.getInjectedPsi();
    if (injectedPsi == null) {
      return editor;
    }
    TextRange hostRange = injectionHost.getTextRange();
    for (Pair<PsiElement, TextRange> pair : injectedPsi) {
      TextRange range = pair.getSecond();
      if (hostRange.cutOut(range).grown(1).contains(offset)) {
        PsiFile injectedFile = pair.getFirst().getContainingFile();
        Document document = PsiDocumentManager.getInstance(editor.getProject()).getDocument(injectedFile);

        return EditorDelegate.create((DocumentRange)document, (EditorImpl)editor, injectedFile);
      }
    }
    return editor;
  }

  public static PsiFile findInjectedPsiAt(PsiFile host, int offset) {
    PsiDocumentManager.getInstance(host.getProject()).commitAllDocuments();
    PsiLanguageInjectionHost injectionHost = findInjectionHost(host.findElementAt(offset));
    if (injectionHost == null && offset != 0) {
      injectionHost = findInjectionHost(host.findElementAt(offset-1));
    }
    List<Pair<PsiElement, TextRange>> injectedPsi = injectionHost == null ? null : injectionHost.getInjectedPsi();
    if (injectedPsi == null) {
      return null;
    }
    TextRange hostRange = injectionHost.getTextRange();
    for (Pair<PsiElement, TextRange> pair : injectedPsi) {
      TextRange range = pair.getSecond();
      if (hostRange.cutOut(range).grown(1).contains(offset)) {
        PsiFile injectedFile = pair.getFirst().getContainingFile();
        return injectedFile;
      }
    }
    return null;
  }

  @Nullable
  public static PsiLanguageInjectionHost findInjectionHost(PsiElement element) {
    if (element == null || element instanceof PsiFile) return null;
    if (element instanceof PsiLanguageInjectionHost) {
      return (PsiLanguageInjectionHost)element;
    }
    element = element.getParent();
    if (element == null || element instanceof PsiFile) return null;
    if (element instanceof PsiLanguageInjectionHost) {
      return (PsiLanguageInjectionHost)element;
    }
    element = element.getParent();
    if (element == null || element instanceof PsiFile) return null;
    if (element instanceof PsiLanguageInjectionHost) {
      return (PsiLanguageInjectionHost)element;
    }
    return null;
  }

  private static class InjectedPsiProvider<T extends PsiLanguageInjectionHost> implements CachedValueProvider<List<Pair<PsiElement, TextRange>>> {
    private SmartPsiElementPointer<T> myHostPointer;
    private final LiteralTextEscaper<T> myTextEscaper;
    private T myHost;

    public InjectedPsiProvider(final T host, @Nullable LiteralTextEscaper<T> textEscaper) {
      myHost = host;
      LOG.assertTrue(host.isValid());
      myTextEscaper = textEscaper;
    }

    private void fastenMyBelts(final T host) {
      if (myHostPointer == null) {
        myHostPointer = createHostSmartPointer(host);
        myHost = null;
      }
    }

    public Result<List<Pair<PsiElement, TextRange>>> compute() {
      final T host = myHostPointer == null ? myHost : myHostPointer.getElement();
      if (host == null) return null;
      final List<Pair<PsiElement, TextRange>> result = queryInjectionHostForPsi(host);
      if (result == null || result.isEmpty()) return null;
      fastenMyBelts(host); // create smart pointer only if necessary
      return new Result<List<Pair<PsiElement, TextRange>>>(result, host, PsiModificationTracker.MODIFICATION_COUNT);
    }

    private List<Pair<PsiElement, TextRange>> queryInjectionHostForPsi(final T host) {
      final TextRange hostRange = host.getTextRange();
      PsiFile hostPsiFile = host.getContainingFile();
      VirtualFile virtualFile = hostPsiFile.getVirtualFile();
      if (virtualFile == null) {
        PsiFile originalFile = hostPsiFile.getOriginalFile();
        if (originalFile != null) virtualFile = originalFile.getVirtualFile();
      }
      if (virtualFile == null) return null;
      final VirtualFile hostVirtualFile = virtualFile;
      final DocumentEx hostDocument = (DocumentEx)hostPsiFile.getViewProvider().getDocument();
      final PsiManagerImpl psiManager = (PsiManagerImpl)host.getManager();
      final List<Pair<PsiElement, TextRange>> result = new SmartList<Pair<PsiElement, TextRange>>();
      InjectedLanguagePlaces placesRegistrar = new InjectedLanguagePlaces() {
        public void addPlace(@NotNull Language language, @NotNull TextRange rangeInsideHost, @Nullable String prefix, @Nullable String suffix) {
          PsiElement psi = parseInjectedPsiFile(host, rangeInsideHost, language, hostVirtualFile, hostRange, hostDocument, myTextEscaper,
                                                prefix == null ? "" : prefix, suffix == null ? "" : suffix);
          if (psi != null) {
            result.add(new Pair<PsiElement,TextRange>(psi, rangeInsideHost));
          }
        }
      };
      for (LanguageInjector injector : psiManager.getLanguageInjectors()) {
        injector.getLanguagesToInject(host, placesRegistrar);
      }
      for (LanguageInjector injector : Extensions.getExtensions(LanguageInjector.EXTENSION_POINT_NAME)) {
        injector.getLanguagesToInject(host, placesRegistrar);
      }
      return result;
    }
  }

  private static <T extends PsiLanguageInjectionHost> SmartPsiElementPointer<T> createHostSmartPointer(final T host) {
    return host.isPhysical()
           ? SmartPointerManager.getInstance(host.getProject()).createSmartPsiElementPointer(host)
           : new SmartPsiElementPointer<T>() {
             public T getElement() {
               return host;
             }
           };
  }

  private static final Key<List<DocumentRange>> INJECTED_DOCS_KEY = Key.create("INJECTED_DOCS_KEY");
  public static void commitAllInjectedDocuments(Document hostDocument, Project project) {
    List<DocumentRange> injected = hostDocument.getUserData(INJECTED_DOCS_KEY);
    if (injected == null) return;
    List<DocumentRange> oldInjected = new ArrayList<DocumentRange>(injected);
    PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
    PsiFile hostPsiFile = documentManager.getPsiFile(hostDocument);
    for (DocumentRange injDocument : oldInjected) {
      final PsiFileImpl oldFile = (PsiFileImpl)documentManager.getCachedPsiFile(injDocument);
      RangeMarker rangeMarker = injDocument.getTextRange();
      if (!rangeMarker.isValid() || oldFile == null) {
        injected.remove(injDocument);
      }
      PsiElement element = hostPsiFile.findElementAt(rangeMarker.getStartOffset());
      PsiLanguageInjectionHost injectionHost = findInjectionHost(element);
      if (injectionHost == null) {
        injected.remove(injDocument);
        continue;
      }
      // it is here reparse happens and old file contents replaced
      List<Pair<PsiElement, TextRange>> result = injectionHost.getInjectedPsi();
      if (result != null) {
        for (Pair<PsiElement, TextRange> pair : result) {
          PsiElement injectedPsi = pair.getFirst();
          PsiFile psiFile = (PsiFile)injectedPsi;
          PsiDocumentManagerImpl.checkConsistency(psiFile, psiFile.getViewProvider().getDocument());
        }
      }
    }
    PsiDocumentManagerImpl.checkConsistency(hostPsiFile, hostDocument);
  }

  private static PsiFile registerDocumentRange(final DocumentRange documentRange, final PsiFile injectedPsi) {
    DocumentEx hostDocument = documentRange.getDelegate();
    List<DocumentRange> injected = hostDocument.getUserData(INJECTED_DOCS_KEY);

    if (injected == null) {
      injected = ((UserDataHolderEx)hostDocument).putUserDataIfAbsent(INJECTED_DOCS_KEY, new ArrayList<DocumentRange>());
    }

    RangeMarker rangeMarker = documentRange.getTextRange();
    TextRange textRange = new TextRange(rangeMarker.getStartOffset(), rangeMarker.getEndOffset());
    PsiDocumentManager documentManager = PsiDocumentManager.getInstance(injectedPsi.getProject());
    for (int i = injected.size()-1; i>=0; i--) {
      DocumentRange oldDocument = injected.get(i);
      PsiFileImpl oldFile = (PsiFileImpl)documentManager.getCachedPsiFile(oldDocument);
      if (oldFile == null) {
        injected.remove(i);
        continue;
      }
      assert oldFile.getViewProvider() instanceof MyFileViewProvider : oldFile.getViewProvider();
      RangeMarker oldRangeMarker = oldDocument.getTextRange();
      TextRange oldTextRange = new TextRange(oldRangeMarker.getStartOffset(), oldRangeMarker.getEndOffset());
      ASTNode injectedNode = injectedPsi.getNode();
      ASTNode oldFileNode = oldFile.getNode();
      assert injectedNode != null;
      assert oldFileNode != null;
      if (oldTextRange.intersects(textRange)) {
        if (!injectedNode.getText().equals(oldFileNode.getText())) {
          // replace psi
          FileElement newFileElement = (FileElement)injectedNode.copyElement();
          FileElement oldFileElement = oldFile.getTreeElement();

          if (oldFileElement.getFirstChildNode() != null) {
            TreeUtil.removeRange(oldFileElement.getFirstChildNode(), null);
          }
          final ASTNode firstChildNode = newFileElement.getFirstChildNode();
          if (firstChildNode != null) {
            TreeUtil.addChildren(oldFileElement, (TreeElement)firstChildNode);
          }
          oldFileElement.setCharTable(newFileElement.getCharTable());
          oldFile.putUserData(ResolveUtil.INJECTED_IN_ELEMENT, injectedPsi.getUserData(ResolveUtil.INJECTED_IN_ELEMENT));
          FileDocumentManagerImpl.registerDocument(documentRange, oldFile.getVirtualFile());
          oldFile.subtreeChanged();

          SingleRootFileViewProvider viewProvider = (SingleRootFileViewProvider)oldFile.getViewProvider();
          viewProvider.setVirtualFile(injectedPsi.getVirtualFile());
        }
        return oldFile;
      }
    }
    injected.add(documentRange);
    return injectedPsi;
  }

  public static Editor openEditorFor(PsiFile file, Project project) {
    Document document = PsiDocumentManager.getInstance(project).getDocument(file);
    // may return editor injected in current selection in the host editor, not for the file passed as argument
    VirtualFile virtualFile = file.getVirtualFile();
    if (virtualFile == null) {
      return null;
    }
    if (virtualFile instanceof VirtualFileDelegate) {
      virtualFile = ((VirtualFileDelegate)virtualFile).getDelegate();
    }
    Editor editor = FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, virtualFile, -1), false);
    if (editor == null || editor instanceof EditorDelegate) return editor;
    if (document instanceof DocumentRange) {
      return EditorDelegate.create((DocumentRange)document,(EditorImpl)editor,file);
    }
    return editor;
  }

  public static PsiFile getContainingInjectedFile(PsiElement element) {
    PsiFile psiFile = element.getContainingFile();
    if (psiFile == null) return null;
    PsiElement host = psiFile.getContext();
    return host == null ? null : psiFile;
  }
  public static boolean isInInjectedLanguagePrefixSuffix(final PsiElement element) {
    PsiFile injectedFile = getContainingInjectedFile(element);
    if (injectedFile == null) return false;
    Document document = PsiDocumentManager.getInstance(element.getProject()).getCachedDocument(injectedFile);
    if (!(document instanceof DocumentRange)) return false;
    DocumentRange documentRange = (DocumentRange)document;
    TextRange elementRange = element.getTextRange();
    TextRange editable = documentRange.intersectWithEditable(elementRange);
    return !elementRange.equals(editable); //) throw new IncorrectOperationException("Can't change "+ UsageViewUtil.createNodeText(element, true));
  }
}