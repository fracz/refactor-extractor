
package com.intellij.codeInsight.daemon.impl.analysis;

import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.codeInsight.daemon.XmlErrorMessages;
import com.intellij.codeInsight.daemon.impl.ShowAutoImportPass;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.HintAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.ide.util.FQNameCellRenderer;
import com.intellij.j2ee.openapi.ex.ExternalResourceManagerEx;
import com.intellij.jsp.impl.TldDescriptor;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.cache.impl.idCache.IdTableBuilding;
import com.intellij.psi.impl.source.jsp.JspManager;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.meta.PsiMetaData;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlExtension;
import com.intellij.xml.impl.schema.AnyXmlElementDescriptor;
import com.intellij.xml.impl.schema.XmlNSDescriptorImpl;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
* User: Maxim.Mossienko
* Date: Nov 29, 2007
* Time: 11:13:33 PM
* To change this template use File | Settings | File Templates.
*/
public class CreateNSDeclarationIntentionFix implements HintAction, LocalQuickFix {
  private final boolean myInJsp;
  @NotNull private final XmlTag myTag;
  private final String myNamespacePrefix;
  @Nullable private final PsiElement myElement;

  public CreateNSDeclarationIntentionFix(@NotNull final XmlTag tag,
                                         @NotNull final String namespacePrefix) {
    this(tag, namespacePrefix, null);
  }

  public CreateNSDeclarationIntentionFix(@NotNull final XmlTag tag,
                                         @NotNull final String namespacePrefix,
                                         @Nullable final PsiElement element) {
    myTag = tag;
    myNamespacePrefix = namespacePrefix;
    myElement = element;
    myInJsp = isJspFile(tag.getContainingFile());
  }

  @NotNull
  public String getText() {
    return XmlErrorMessages.message(
      myInJsp ?
      "create.taglib.declaration.quickfix":
      "create.namespace.declaration.quickfix"
    );
  }

  @NotNull
  public String getName() {
    return getFamilyName();
  }

  @NotNull
  public String getFamilyName() {
    return getText();
  }

  public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
    final PsiFile containingFile = descriptor.getPsiElement().getContainingFile();
    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
    final PsiFile file = editor != null ? PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument()):null;
    if (file == null || file.getVirtualFile() != containingFile.getVirtualFile()) return;

    try { invoke(project, editor, containingFile); } catch (IncorrectOperationException ex) { ex.printStackTrace(); }
  }

  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    return true;
  }

  private static boolean isJspFile(PsiFile file) {
    return file.getFileType() == StdFileTypes.JSP;
  }

  private static String[] guessNamespace(final PsiFile file, String name) {
    final Set<String> possibleUris = new LinkedHashSet<String>();
    final ExternalUriProcessor processor = new ExternalUriProcessor() {
      public void process(@NotNull String ns, final String url) {
        possibleUris.add(ns);
      }
    };

    processExternalUris(new TagMetaHandler(name), file, processor);

    return possibleUris.toArray( new String[possibleUris.size()] );
  }

  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    if (!CodeInsightUtil.prepareFileForWrite(file)) return;

    String[] namespaces;
    if (!myInJsp) {
      namespaces = guessNamespace(file, myElement == null ? myTag.getLocalName() : myElement.getText());
      if (file.getLanguage() == StdLanguages.JSPX) {
        final Map<String, String> map = guessJspNamespaces();
        final Set<String> strings = map.keySet();
        namespaces = ArrayUtil.mergeArrays(namespaces, strings.toArray(new String[strings.size()]), String.class);
      }
    } else {
      final Map<String, String> map = guessJspNamespaces();
      final Set<String> strings = map.keySet();
      namespaces = strings.toArray(new String[strings.size()]);
    }
    Arrays.sort(namespaces);

    runActionOverSeveralAttributeValuesAfterLettingUserSelectTheNeededOne(
      namespaces,
      project,
      new StringToAttributeProcessor() {
        public void doSomethingWithGivenStringToProduceXmlAttributeNowPlease(@NotNull final String namespace) throws IncorrectOperationException {

          final int offset = editor.getCaretModel().getOffset();
          final RangeMarker marker = editor.getDocument().createRangeMarker(offset, offset);
          final XmlExtension extension = XmlExtension.getExtension((XmlFile)file);
          extension.insertNamespaceDeclaration((XmlFile)file, editor, Collections.singleton(namespace), myNamespacePrefix, new XmlExtension.Runner<String, IncorrectOperationException>() {
            public void run(final String param) throws IncorrectOperationException {
              if (namespace.length() > 0) {
                editor.getCaretModel().moveToOffset(marker.getStartOffset());
              }
            }
          });
        }
      },
      XmlErrorMessages.message(myInJsp ? "select.taglib.title":"select.namespace.title"),
      this,
      editor);
  }

  public boolean startInWriteAction() {
    return true;
  }

  public boolean showHint(final Editor editor) {
    final Map<String, String> namespaces = guessJspNamespaces();
    if (!namespaces.isEmpty()) {
      final String message = ShowAutoImportPass.getMessage(namespaces.size() > 1, namespaces.keySet().iterator().next());
      final String title = XmlErrorMessages.message(myInJsp ? "select.taglib.title" : "select.namespace.title");
      final JspFile jspFile = PsiUtil.getJspFile(myTag);
      final PsiElement element = myElement == null ? myTag : myElement;
      final ImportNSAction action = new ImportNSAction(namespaces, jspFile, myElement != null ? null : myTag, editor, title);
      HintManager.getInstance().showQuestionHint(editor, message, element.getTextOffset(), element.getTextRange().getEndOffset(), action);
      return true;
    }
    return false;
  }

  @NotNull
  public Map<String,String> guessJspNamespaces() {

    if (!myTag.isValid()) {
      return Collections.emptyMap();
    }
    final JspFile jspFile = PsiUtil.getJspFile(myTag);
    if (jspFile == null) {
      return Collections.emptyMap();
    }
    final String text = myTag.getName().trim();
    final int pos = text.indexOf(':');
    final Map<String, String> namespaces = new LinkedHashMap<String,String>();
    final Project project = myTag.getProject();
    final JspManager jspManager = JspManager.getInstance(project);
    if (myElement != null) {
      final Set<String> map = jspManager.getNamespacesByFunctionName(myElement.getText(), jspFile, false);
      for (String uri : map) {
        final String prefix = StringUtil.isEmpty(myNamespacePrefix) ? jspManager.getPrefixForNamespace(uri, jspFile) : myNamespacePrefix;
        namespaces.put(uri, prefix);
      }
    } else if (pos == -1) {  // tag name
      final Set<String> map = jspManager.getNamespacesByTagName(text, jspFile, false);
      for (String uri : map) {
        final String prefix = StringUtil.isEmpty(myNamespacePrefix) ? jspManager.getPrefixForNamespace(uri, jspFile) : myNamespacePrefix;
        namespaces.put(uri, prefix);
      }
    } else if (pos == text.length() - 1) { // ns prefix
      final Module module = ModuleUtil.findModuleForPsiElement(myTag);
      if (module == null) {
        return Collections.emptyMap();
      }
      final String prefix = text.substring(0, text.length() - 1);
      final Collection<XmlFile> files = jspManager.getPossibleTldFiles(module);
      for (XmlFile psiFile : files) {
        final XmlDocument document = psiFile.getDocument();
        if (document != null) {
          final TldDescriptor descriptor = (TldDescriptor)document.getMetaData();
          if (descriptor != null) {
            final String defaultPrefix = descriptor.getDefaultPrefix();
            if (defaultPrefix != null && defaultPrefix.equals(prefix)) {
              final String uri = descriptor.getUri();
              if (!StringUtil.isEmpty(uri)) {
                namespaces.put(uri, prefix);
              }
            }
          }
        }
      }

    } else {  // qualified tag name
      final String tagName = text.substring(pos+1);
      final Set<String> map = jspManager.getNamespacesByTagName(tagName, jspFile, false);
      for (String uri : map) {
        namespaces.put(uri, jspManager.getPrefixForNamespace(uri, jspFile));
      }
    }
    return namespaces;
  }

  private static boolean checkIfGivenXmlHasTheseWords(final String name, final XmlFile tldFileByUri) {
    if (name == null || name.length() == 0) return true;
    final List<String> list = StringUtil.getWordsIn(name);
    final String[] words = list.toArray(new String[list.size()]);
    final boolean[] wordsFound = new boolean[words.length];
    final int[] wordsFoundCount = new int[1];

    IdTableBuilding.ScanWordProcessor wordProcessor = new IdTableBuilding.ScanWordProcessor() {
      public void run(final CharSequence chars, int start, int end, char[] charArray) {
        if (wordsFoundCount[0] == words.length) return;
        final int foundWordLen = end - start;

        Next:
        for (int i = 0; i < words.length; ++i) {
          final String localName = words[i];
          if (wordsFound[i] || localName.length() != foundWordLen) continue;

          for (int j = 0; j < localName.length(); ++j) {
            if (chars.charAt(start + j) != localName.charAt(j)) continue Next;
          }

          wordsFound[i] = true;
          wordsFoundCount[0]++;
          break;
        }
      }
    };

    final CharSequence contents = tldFileByUri.getViewProvider().getContents();

    IdTableBuilding.scanWords(wordProcessor, contents, 0, contents.length());

    return wordsFoundCount[0] == words.length;
  }

  public interface StringToAttributeProcessor {
    void doSomethingWithGivenStringToProduceXmlAttributeNowPlease(@NonNls @NotNull String attrName) throws IncorrectOperationException;
  }


  public static void runActionOverSeveralAttributeValuesAfterLettingUserSelectTheNeededOne(final @NotNull String[] namespacesToChooseFrom,
                                                                                           final Project project, final StringToAttributeProcessor onSelection,
                                                                                           String title,
                                                                                           final IntentionAction requestor,
                                                                                           final Editor editor) throws IncorrectOperationException {

    if (namespacesToChooseFrom.length > 1 && !ApplicationManager.getApplication().isUnitTestMode()) {
      final JList list = new JList(namespacesToChooseFrom);
      list.setCellRenderer(new FQNameCellRenderer());
      Runnable runnable = new Runnable() {
        public void run() {
          final int index = list.getSelectedIndex();
          if (index < 0) return;
          PsiDocumentManager.getInstance(project).commitAllDocuments();

          CommandProcessor.getInstance().executeCommand(
            project,
            new Runnable() {
              public void run() {
                ApplicationManager.getApplication().runWriteAction(
                  new Runnable() {
                    public void run() {
                      try {
                        onSelection.doSomethingWithGivenStringToProduceXmlAttributeNowPlease(namespacesToChooseFrom[index]);
                      } catch (IncorrectOperationException ex) {
                        throw new RuntimeException(ex);
                      }
                    }
                  }
                );
              }
            },
            requestor.getText(),
            requestor.getFamilyName()
          );
        }
      };

      new PopupChooserBuilder(list).
        setTitle(title).
        setItemChoosenCallback(runnable).
        createPopup().
        showInBestPositionFor(editor);
    } else {
      onSelection.doSomethingWithGivenStringToProduceXmlAttributeNowPlease(namespacesToChooseFrom.length == 0 ? "" : namespacesToChooseFrom[0]);
    }
  }

  public static void processExternalUris(final MetaHandler metaHandler,
                                 final PsiFile file,
                                 final ExternalUriProcessor processor) {
    if (ApplicationManager.getApplication().isUnitTestMode()) processExternalUrisImpl(metaHandler, file, processor);
    else {
      ProgressManager.getInstance().runProcessWithProgressSynchronously(
        new Runnable() {
          public void run() {
            processExternalUrisImpl(metaHandler, file, processor);
          }
        },
        XmlErrorMessages.message("finding.acceptable.uri"),
        false,
        file.getProject()
      );
    }
  }

  public interface MetaHandler {
    boolean isAcceptableMetaData(PsiMetaData metadata, final String url);
    String searchFor();
  }

  public static class TagMetaHandler implements MetaHandler {
    private final String myName;


    public TagMetaHandler(final String name) {
      myName = name;
    }

    public boolean isAcceptableMetaData(final PsiMetaData metaData, final String url) {
      if (metaData instanceof XmlNSDescriptorImpl) {
        final XmlNSDescriptorImpl nsDescriptor = (XmlNSDescriptorImpl)metaData;

        final XmlElementDescriptor descriptor = nsDescriptor.getElementDescriptor(searchFor(), url);
        return descriptor != null && !(descriptor instanceof AnyXmlElementDescriptor);
      }
      return false;
    }

    public String searchFor() {
      return myName;
    }
  }

  private static void processExternalUrisImpl(final MetaHandler metaHandler,
                                              final PsiFile file,
                                              final ExternalUriProcessor processor) {
    final ProgressIndicator pi = ProgressManager.getInstance().getProgressIndicator();

    final String searchFor = metaHandler.searchFor();

    if (pi != null) pi.setText(XmlErrorMessages.message("looking.in.schemas"));
    final ExternalResourceManagerEx instanceEx = ExternalResourceManagerEx.getInstanceEx();
    final String[] availableUrls = instanceEx.getResourceUrls(null, true);
    int i = 0;

    for (String url : availableUrls) {
      if (pi != null) {
        pi.setFraction((double)i / availableUrls.length);
        pi.setText2(url);
        ++i;
      }
      final XmlFile xmlFile = XmlUtil.findNamespace(file, url);

      if (xmlFile != null) {
        final boolean wordFound = checkIfGivenXmlHasTheseWords(searchFor, xmlFile);
        if (!wordFound) continue;
        final PsiMetaData metaData = xmlFile.getDocument().getMetaData();

        if (metaHandler.isAcceptableMetaData(metaData, url)) {
          final XmlNSDescriptorImpl descriptor = metaData instanceof XmlNSDescriptorImpl ? (XmlNSDescriptorImpl)metaData:null;
          final String defaultNamespace = descriptor != null ? descriptor.getDefaultNamespace():url;

          // Skip rare stuff
          if (!XmlUtil.XML_SCHEMA_URI2.equals(defaultNamespace) && !XmlUtil.XML_SCHEMA_URI3.equals(defaultNamespace)) {
            processor.process(defaultNamespace, url);
          }
        }
      }
    }
  }

  public interface ExternalUriProcessor {
    void process(@NotNull String uri,@Nullable final String url);
  }
}