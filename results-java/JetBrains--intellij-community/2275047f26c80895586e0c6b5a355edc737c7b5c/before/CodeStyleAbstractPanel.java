/*
 * Copyright (c) 2004 JetBrains s.r.o. All  Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * -Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduct the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of JetBrains or IntelliJ IDEA
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. JETBRAINS AND ITS LICENSORS SHALL NOT
 * BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT
 * OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL JETBRAINS OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN
 * IF JETBRAINS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 */
package com.intellij.application.options;

import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.util.IncorrectOperationException;

import javax.swing.*;

public abstract class CodeStyleAbstractPanel {
  private static Logger LOG = Logger.getInstance("#com.intellij.application.options.CodeStyleXmlPanel");
  protected final Editor myEditor;
  protected final CodeStyleSettings mySettings;
  private boolean myShouldUpdatePreview;
  protected final static int[] ourWrappings = new int[] {CodeStyleSettings.DO_NOT_WRAP,
                                                       CodeStyleSettings.WRAP_AS_NEEDED,
                                                       CodeStyleSettings.WRAP_ON_EVERY_ITEM,
                                                       CodeStyleSettings.WRAP_ALWAYS};
  private long myLastDocumentModificationStamp;
  private String myTextToReformat = null;

  public CodeStyleAbstractPanel(CodeStyleSettings settings) {
    mySettings = settings;
    myEditor = createEditor();
  }

  protected final Editor createEditor() {
    EditorFactory editorFactory = EditorFactory.getInstance();
    myTextToReformat = getPreviewText();
    Document editorDocument = editorFactory.createDocument(myTextToReformat);
    EditorEx editor = (EditorEx)editorFactory.createEditor(editorDocument);

    myLastDocumentModificationStamp = editor.getDocument().getModificationStamp();

    EditorSettings editorSettings = editor.getSettings();
    editorSettings.setWhitespacesShown(true);
    editorSettings.setLineMarkerAreaShown(false);
    editorSettings.setLineNumbersShown(false);
    editorSettings.setFoldingOutlineShown(false);
    editorSettings.setAdditionalColumnsCount(0);
    editorSettings.setAdditionalLinesCount(1);
    editorSettings.setRightMargin(mySettings.RIGHT_MARGIN);

    EditorColorsScheme scheme = editor.getColorsScheme();
    scheme.setColor(EditorColors.CARET_ROW_COLOR, null);

    editor.setHighlighter(HighlighterFactory.createXMLHighlighter(scheme));

    return editor;
  }

  protected final void updatePreview() {
    if (!myShouldUpdatePreview) {
      return;
    }

    if (myLastDocumentModificationStamp != myEditor.getDocument().getModificationStamp()) {
      myTextToReformat = myEditor.getDocument().getText();
    }

    CommandProcessor.getInstance().executeCommand(ProjectManager.getInstance().getDefaultProject(),
      new Runnable() {
        public void run() {
          replaceText();
        }
      }, null, null);

    myLastDocumentModificationStamp = myEditor.getDocument().getModificationStamp();
  }

  private void replaceText() {
    final Project project = ProjectManager.getInstance().getDefaultProject();
    final PsiManager manager = PsiManager.getInstance(project);
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        PsiElementFactory factory = manager.getElementFactory();
        try {
          PsiFile psiFile = factory.createFileFromText("a." + getFileType().getDefaultExtension(), myTextToReformat);

          CodeStyleSettings clone = (CodeStyleSettings)mySettings.clone();
          apply(clone);

          CodeStyleSettingsManager.getInstance(project).setTemporarySettings(clone);
          CodeStyleManager.getInstance(project).reformat(psiFile);
          CodeStyleSettingsManager.getInstance(project).dropTemporarySettings();

          myEditor.getSettings().setTabSize(clone.getTabSize(getFileType()));
          Document document = myEditor.getDocument();
          document.replaceString(0, document.getTextLength(), psiFile.getText());
        }
        catch (IncorrectOperationException e) {
          LOG.error(e);
        }
      }

    });
  }

  protected abstract FileType getFileType();

  protected abstract String getPreviewText();

  public abstract void apply(CodeStyleSettings settings);

  public final void reset() {
    myShouldUpdatePreview = false;
    try {
      resetImpl();
    }
    finally {
      myShouldUpdatePreview = true;
      updatePreview();
    }
  }

  protected int getIndexForWrapping(int value) {
    for (int i = 0; i < ourWrappings.length; i++) {
      int ourWrapping = ourWrappings[i];
      if (ourWrapping == value) return i;
    }
    LOG.assertTrue(false);
    return 0;
  }

  public abstract boolean isModified(CodeStyleSettings settings);

  public abstract JComponent getPanel();

  public final void dispose() {
    EditorFactory.getInstance().releaseEditor(myEditor);
  }

  protected abstract void resetImpl();

  protected void fillWrappingCombo(final JComboBox wrapCombo) {
    wrapCombo.addItem("Do not wrap");
    wrapCombo.addItem("Wrap if long");
    wrapCombo.addItem("Chop down if long");
    wrapCombo.addItem("Wrap always");
  }
}