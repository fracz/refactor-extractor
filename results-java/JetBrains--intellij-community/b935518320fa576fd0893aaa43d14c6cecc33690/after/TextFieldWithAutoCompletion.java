package org.jetbrains.plugins.ruby.rake.runConfigurations;

import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.PresentableLookupValue;
import com.intellij.ide.IdeBundle;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.ui.EditorTextField;
import com.intellij.util.LocalTimeCounter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Roman Chernyatchik
 */
public class TextFieldWithAutoCompletion extends EditorTextField {
  private List<LookupItem<PresentableLookupValue>> myLookupItems;

  public TextFieldWithAutoCompletion(final Project project) {
    super(createDocument(project), project, PlainTextLanguage.INSTANCE.getAssociatedFileType());

    new VariantsCompletionAction();
  }

  private static Document createDocument(final Project project) {
    final Language language = PlainTextLanguage.INSTANCE;

    final PsiFileFactory factory = PsiFileFactory.getInstance(project);
    final FileType fileType = language.getAssociatedFileType();
    assert fileType != null;

    final long stamp = LocalTimeCounter.currentTime();
    final PsiFile psiFile = factory.createFileFromText("Dummy." + fileType.getDefaultExtension(), fileType, "", stamp, true, false);
    final Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
    assert document != null;
    return document;
  }

  private class VariantsCompletionAction extends AnAction {
    private VariantsCompletionAction() {
      final AnAction action = ActionManager.getInstance().getAction(IdeActions.ACTION_CODE_COMPLETION);
      if (action != null) {
        registerCustomShortcutSet(action.getShortcutSet(), TextFieldWithAutoCompletion.this);
      }
    }

    public void actionPerformed(final AnActionEvent e) {
      calcItemsAndShowPopup();
    }
  }

  public void setVariants(@Nullable final List<PresentableLookupValue> variants) {
    if (variants == null) {
      myLookupItems = Collections.emptyList();
    } else {
      myLookupItems = new ArrayList<LookupItem<PresentableLookupValue>>(variants.size());
      for (PresentableLookupValue variant : variants) {
        myLookupItems.add(new LookupItem<PresentableLookupValue>(variant, variant.getPresentation()));
      }
    }
  }

  private void calcItemsAndShowPopup() {
    final LookupItem<PresentableLookupValue>[] items = calcLookupItems(getPrefix());
    if (items.length == 0) {
      showNoSuggestionsPopup();
      return;
    }

    showCompletionPopup(items, null);
  }

  private LookupItem<PresentableLookupValue>[] calcLookupItems(final String prefix) {
    final List<LookupItem<PresentableLookupValue>> items = new ArrayList<LookupItem<PresentableLookupValue>>();
    if (TextUtil.isEmpty(prefix)) {
      for (LookupItem<PresentableLookupValue> lookupItem : myLookupItems) {
        items.add(lookupItem);
      }
    } else {
      final String regexp = NameUtil.buildRegexp(prefix, 0, true, true);
      final Pattern pattern = Pattern.compile(regexp);
      final Matcher matcher = pattern.matcher("");

      for (LookupItem<PresentableLookupValue> lookupItem : myLookupItems) {
        matcher.reset(lookupItem.getLookupString());
        if (matcher.matches()) {
          items.add(lookupItem);
        }
      }
    }

    Collections.sort(items, new Comparator<LookupItem<PresentableLookupValue>>() {
      public int compare(final LookupItem<PresentableLookupValue> item1,
                         final LookupItem<PresentableLookupValue> item2) {
        return item1.getLookupString().compareTo(item2.getLookupString());
      }
    });

    return (LookupItem<PresentableLookupValue>[])items.toArray(new LookupItem[items.size()]);
  }

  private String getPrefix() {
    return getText().substring(0, getCaretModel().getOffset());
  }

  private void showNoSuggestionsPopup() {
    // hide active popup
    LookupManager.getInstance(getProject()).hideActiveLookup();

    final JLabel message = HintUtil.createErrorLabel(IdeBundle.message("file.chooser.completion.no.suggestions"));
    final ComponentPopupBuilder builder = JBPopupFactory.getInstance().createComponentPopupBuilder(message, message);
    builder.setRequestFocus(false).setResizable(false).setAlpha(0.1f).setFocusOwners(new Component[] {this});
    builder.createPopup().showUnderneathOf(this);
  }

  private void showCompletionPopup(final LookupItem[] lookupItems, final String title) {
    final Editor editor = getEditor();
    assert editor != null;

    editor.getSelectionModel().removeSelection();
    LookupManager.getInstance(getProject()).showLookup(editor, lookupItems, getPrefix(), null, title);
  }
}