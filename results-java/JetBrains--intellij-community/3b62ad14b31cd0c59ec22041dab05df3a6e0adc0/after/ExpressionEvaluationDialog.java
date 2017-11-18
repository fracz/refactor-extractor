package com.intellij.debugger.ui;

import com.intellij.debugger.DebuggerInvocationUtil;
import com.intellij.debugger.actions.EvaluateAction;
import com.intellij.debugger.engine.evaluation.TextWithImports;
import com.intellij.debugger.impl.PositionUtil;
import com.intellij.debugger.settings.DebuggerSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ExpressionEvaluationDialog extends EvaluationDialog {

  public ExpressionEvaluationDialog(Project project, TextWithImports defaultExpression) {
    super(project, makeOnLine(defaultExpression));
    setTitle("Expression Evaluation");

    final KeyStroke expressionStroke = KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.ALT_MASK);
    final KeyStroke resultStroke = KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.ALT_MASK);

    final JRootPane rootPane = getRootPane();

    final AnAction anAction = new AnAction() {
      public void actionPerformed(AnActionEvent e) {
        getExpressionCombo().requestFocus();
      }
    };
    anAction.registerCustomShortcutSet(new CustomShortcutSet(expressionStroke), rootPane);
    addDisposeRunnable(new Runnable() {
      public void run() {
        anAction.unregisterCustomShortcutSet(rootPane);
      }
    });

    final AnAction anAction2 = new AnAction() {
      public void actionPerformed(AnActionEvent e) {
        getEvaluationPanel().getWatchTree().requestFocus();
      }
    };
    anAction2.registerCustomShortcutSet(new CustomShortcutSet(resultStroke), rootPane);
    addDisposeRunnable(new Runnable() {
      public void run() {
        anAction2.unregisterCustomShortcutSet(rootPane);
      }
    });

    this.init();
  }

  protected DebuggerExpressionComboBox createEditor() {
    return new DebuggerExpressionComboBox(getProject(), PositionUtil.getContextElement(getDebuggerContext()), "evaluation");
  }

  protected JComponent createCenterPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    final JLabel expressionLabel = new JLabel("Expression:");
    expressionLabel.setDisplayedMnemonic('E');
    panel.add(expressionLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
    panel.add(getExpressionCombo(), new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));

    final JLabel resultLabel = new JLabel("Result:");
    resultLabel.setDisplayedMnemonic('R');
    panel.add(resultLabel, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
    panel.add(getEvaluationPanel(), new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 0, 0, 0), 0, 0));

    return panel;
  }

  private static TextWithImports makeOnLine(TextWithImports text) {
    String initialExpression = text.getText();
    if (initialExpression != null) {
      int size = initialExpression.length();
      StringBuffer buf = new StringBuffer(size);
      for (int idx = 0; idx < size; idx++) {
        char ch = initialExpression.charAt(idx);
        if (ch != '\n' && ch != '\r') {
          buf.append(ch);
        }
      }
      text.setText(initialExpression);
    }
    return text;
  }

  protected void initDialogData(TextWithImports text) {
    super.initDialogData(text);
    getExpressionCombo().selectAll();
  }

  private DebuggerExpressionComboBox getExpressionCombo() {
    return (DebuggerExpressionComboBox)getEditor();
  }

  protected Action[] createActions() {
    return new Action[] { getOKAction(), getCancelAction(), new SwitchAction()} ;
  }

  private class SwitchAction extends AbstractAction {
    public SwitchAction() {
      putValue(Action.NAME, "Code Fragment Mode");
    }

    public void actionPerformed(ActionEvent e) {
      final TextWithImports text = getEditor().getText();
      doCancelAction();
      DebuggerInvocationUtil.invokeLater(getProject(), new Runnable() {
        public void run() {
          EvaluateAction.showEvaluationDialog(getProject(), text, DebuggerSettings.EVALUATE_FRAGMENT);
        }
      });
    }
  }

}