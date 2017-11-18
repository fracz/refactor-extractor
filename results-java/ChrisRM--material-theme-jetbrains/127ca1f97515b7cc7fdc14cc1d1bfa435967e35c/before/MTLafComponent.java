/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Chris Magnussen and Elior Boukhobza
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */

package com.chrisrm.idea.themes;

import com.chrisrm.idea.MTConfig;
import com.chrisrm.idea.config.BeforeConfigNotifier;
import com.chrisrm.idea.config.ConfigNotifier;
import com.chrisrm.idea.config.ui.MTForm;
import com.chrisrm.idea.messages.MaterialThemeBundle;
import com.chrisrm.idea.ui.*;
import com.chrisrm.idea.utils.MTUiUtils;
import com.chrisrm.idea.utils.UIReplacer;
import com.intellij.ide.ui.LafManager;
import com.intellij.openapi.actionSystem.impl.ChameleonAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.CaptionPanel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.ScrollUtil;
import javassist.*;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Component for working on the Material Look And Feel
 */
public final class MTLafComponent extends JBPanel implements ApplicationComponent {

  private boolean willRestartIde = false;

  static {
    //    patchUIUtil();
    hackTitleLabel();
    hackIdeaActionButton();
  }

  private MessageBusConnection connect;

  public MTLafComponent(final LafManager lafManager) {
    lafManager.addLafManagerListener(source -> installMaterialComponents());
  }

  @Override
  public void initComponent() {
    installMaterialComponents();

    // Patch UI components
    UIReplacer.patchUI();

    // Listen for changes on the settings
    connect = ApplicationManager.getApplication().getMessageBus().connect();
    connect.subscribe(ConfigNotifier.CONFIG_TOPIC, this::onSettingsChanged);
    connect.subscribe(BeforeConfigNotifier.BEFORE_CONFIG_TOPIC, (this::onBeforeSettingsChanged));
  }

  public static void patchUIUtil() {
    // Hack method
    try {
      final ClassPool cp = new ClassPool(true);
      cp.insertClassPath(new ClassClassPath(ScrollUtil.class));
      final CtClass ctClass = cp.get("com.intellij.util.ui.UIUtil");
      final CtMethod ctMethod = ctClass.getDeclaredMethod("drawHeader");
      ctMethod.instrument(new ExprEditor() {
        @Override
        public void edit(final ConstructorCall c) throws CannotCompileException {
          try {
            if (c.getConstructor().getLongName().equals("java.awt.Color")) {
              c.replace("{ $_ = javax.swing.UIManager.getColor(\"activeCaption\"); }");
            }
          } catch (final NotFoundException e) {
            e.printStackTrace();
          }
        }
      });
      ctClass.writeFile();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * For better dialog titles (since I have no idea how to know when dialogs appear, I can't attach events so I'm directly hacking
   * the source code). I hate doing this.
   */
  public static void hackTitleLabel() {
    // Hack method
    try {
      final ClassPool cp = new ClassPool(true);
      cp.insertClassPath(new ClassClassPath(CaptionPanel.class));
      final CtClass ctClass = cp.get("com.intellij.ui.TitlePanel");
      final CtConstructor declaredConstructor = ctClass.getDeclaredConstructor(new CtClass[]{
          cp.get("javax.swing.Icon"),
          cp.get("javax.swing" +
              ".Icon")});
      declaredConstructor.instrument(new ExprEditor() {
        @Override
        public void edit(final MethodCall m) throws CannotCompileException {
          if (m.getMethodName().equals("empty")) {
            // Replace insets
            m.replace("{ $1 = 10; $2 = 10; $3 = 10; $4 = 10; $_ = $proceed($$); }");
          } else if (m.getMethodName().equals("setHorizontalAlignment")) {
            // Set title at the left
            m.replace("{ $1 = javax.swing.SwingConstants.LEFT; $_ = $proceed($$); }");
          } else if (m.getMethodName().equals("setBorder")) {
            // Bigger heading
            m.replace("{ $_ = $proceed($$); myLabel.setFont(myLabel.getFont().deriveFont(1, com.intellij.util.ui.JBUI.scale(16.0f))); }");
          }
        }
      });
      ctClass.toClass();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Change Look and feel of Action buttons
   */
  private static void hackIdeaActionButton() {
    try {
      final ClassPool cp = new ClassPool(true);
      cp.insertClassPath(new ClassClassPath(ChameleonAction.class));
      final CtClass ctClass = cp.get("com.intellij.openapi.actionSystem.impl.IdeaActionButtonLook");

      // Edit paintborder
      final CtClass[] paintBorderParams = new CtClass[]{
          cp.get("java.awt.Graphics"),
          cp.get("java.awt.Dimension"),
          cp.get("int")
      };
      final CtMethod paintBorder = ctClass.getDeclaredMethod("paintBorder", paintBorderParams);
      paintBorder.instrument(new ExprEditor() {
        @Override
        public void edit(final MethodCall m) throws CannotCompileException {
          if (m.getMethodName().equals("setColor")) {
            m.replace("{ $1 = javax.swing.UIManager.getColor(\"Focus.color\"); $_ = $proceed($$); }");
          } else if (m.getMethodName().equals("draw")) {
            m.replace("{ if ($1.getBounds().width > 30) { " +
                "$proceed($$); " +
                "} else { " +
                "$0.fillOval(1, 1, $1.getBounds().width, $1.getBounds().height); } " +
                "}");
          }
        }
      });

      // Edit paintborder
      final CtClass[] paintBackgroundParams = new CtClass[]{
          cp.get("java.awt.Graphics"),
          cp.get("java.awt.Dimension"),
          cp.get("java.awt.Color"),
          cp.get("int")
      };
      final CtMethod paintBackground = ctClass.getDeclaredMethod("paintBackground", paintBackgroundParams);
      paintBackground.instrument(new ExprEditor() {
        @Override
        public void edit(final MethodCall m) throws CannotCompileException {
          if (m.getMethodName().equals("setColor")) {
            m.replace("{ $1 = new java.awt.Color(0x00000000, true); $_ = $proceed($$); }");
          }
        }
      });

      ctClass.toClass();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }


  @Override
  public void disposeComponent() {
    connect.disconnect();
  }

  @NotNull
  @Override
  public String getComponentName() {
    return this.getClass().getName();
  }

  /**
   * Called when MT Config settings are changeds
   *
   * @param mtConfig
   * @param form
   */
  private void onBeforeSettingsChanged(final MTConfig mtConfig, final MTForm form) {
    // Force restart if material design is switched
    restartIdeIfNecessary(mtConfig, form);
  }

  /**
   * Called when MT Config settings are changeds
   *
   * @param mtConfig
   */
  private void onSettingsChanged(final MTConfig mtConfig) {
    // Trigger file icons and statuses update
    MTThemeManager.getInstance().updateFileIcons();

    if (this.willRestartIde) {
      MTUiUtils.restartIde();
    }
  }

  /**
   * Restart IDE if necessary (ex: material design components)
   *
   * @param mtConfig
   * @param form
   */
  private void restartIdeIfNecessary(final MTConfig mtConfig, final MTForm form) {
    // Restart the IDE if changed
    if (mtConfig.needsRestart(form)) {
      final String title = MaterialThemeBundle.message("mt.restartDialog.title");
      final String message = MaterialThemeBundle.message("mt.restartDialog.content");

      final int answer = Messages.showYesNoDialog(message, title, Messages.getQuestionIcon());
      if (answer == Messages.YES) {
        this.willRestartIde = true;
      }
    }
  }

  /**
   * Hack SearchTextField to override SDK's createUI
   */
  private static void hackSearchTextField() throws NotFoundException, CannotCompileException {
    final ClassPool cp = ClassPool.getDefault();
    cp.insertClassPath(new ClassClassPath(MTTextFieldUI.class));

    final CtClass darculaClass = cp.get("com.intellij.ide.ui.laf.darcula.ui.DarculaTextFieldUI");
    final CtClass componentClass = cp.get("javax.swing.JComponent");
    final CtMethod createUI = darculaClass.getDeclaredMethod("createUI", new CtClass[]{componentClass});
    createUI.setBody("{ return com.chrisrm.idea.ui.MTTextFieldFactory.newInstance($1); }");
    darculaClass.toClass();
  }

  /**
   * Replace Table headers
   */
  private void replaceTableHeaders() {
    UIManager.put("TableHeaderUI", MTTableHeaderUI.class.getName());
    UIManager.getDefaults().put(MTTableHeaderUI.class.getName(), MTTableHeaderUI.class);

    UIManager.put("TableHeader.border", new MTTableHeaderBorder());
  }

  /**
   * Replace progress bar
   */
  private void replaceProgressBar() {
    UIManager.put("ProgressBarUI", MTProgressBarUI.class.getName());
    UIManager.getDefaults().put(MTProgressBarUI.class.getName(), MTProgressBarUI.class);

    UIManager.put("ProgressBar.border", new MTProgressBarBorder());
  }

  /**
   * Replace text fields
   */
  private void replaceTextFields() {
    UIManager.put("TextFieldUI", MTTextFieldUI.class.getName());
    UIManager.getDefaults().put(MTTextFieldUI.class.getName(), MTTextFieldUI.class);

    UIManager.put("PasswordFieldUI", MTPasswordFieldUI.class.getName());
    UIManager.getDefaults().put(MTPasswordFieldUI.class.getName(), MTPasswordFieldUI.class);

    UIManager.put("TextField.border", new MTTextBorder());
    UIManager.put("PasswordField.border", new MTTextBorder());
  }

  /**
   * Replace buttons
   */
  private void replaceButtons() {
    UIManager.put("ButtonUI", MTButtonUI.class.getName());
    UIManager.getDefaults().put(MTButtonUI.class.getName(), MTButtonUI.class);

    UIManager.put("Button.border", new MTButtonPainter());
  }

  /**
   * Install Material Design components
   */
  private void installMaterialComponents() {
    final MTConfig mtConfig = MTConfig.getInstance();

    if (mtConfig.getIsMaterialDesign()) {
      replaceButtons();
      replaceTextFields();
      replaceDropdowns();
      replaceProgressBar();
      replaceTree();
      replaceTableHeaders();
      replaceTables();
      replaceStatusBar();
      replaceSpinners();
      replaceCheckboxes();
      //      replaceTextAreas();
    }
  }

  private void replaceCheckboxes() {
    UIManager.put("CheckBoxUI", MTCheckBoxUI.class.getName());
    UIManager.getDefaults().put(MTCheckBoxUI.class.getName(), MTCheckBoxUI.class);

    UIManager.put("CheckBox.border", new MTCheckBoxBorder());
  }

  private void replaceTextAreas() {
    UIManager.put("TextAreaUI", MTTextAreaUI.class.getName());
    UIManager.getDefaults().put(MTTextAreaUI.class.getName(), MTTextAreaUI.class);
  }

  private void replaceDropdowns() {
    UIManager.put("ComboBoxUI", MTComboBoxUI.class.getName());
    UIManager.getDefaults().put(MTComboBoxUI.class.getName(), MTComboBoxUI.class);
  }

  private void replaceSpinners() {
    UIManager.put("SpinnerUI", MTSpinnerUI.class.getName());
    UIManager.getDefaults().put(MTSpinnerUI.class.getName(), MTSpinnerUI.class);

    UIManager.put("Spinner.border", new MTSpinnerBorder());
  }

  private void replaceTables() {
    UIManager.put("Table.focusCellHighlightBorder", new MTTableSelectedCellHighlightBorder());
  }

  private void replaceStatusBar() {
    final MessageBusConnection connect = ApplicationManager.getApplication().getMessageBus().connect();

    // On app init, set the statusbar borders
    connect.subscribe(ProjectManager.TOPIC, new ProjectManagerListener() {
      @Override
      public void projectOpened(@Nullable final Project projectFromCommandLine) {
        MTThemeManager.getInstance().setStatusBarBorders();
      }
    });

    // And also on config change
    connect.subscribe(ConfigNotifier.CONFIG_TOPIC, mtConfig -> MTThemeManager.getInstance().setStatusBarBorders());
  }

  /**
   * Replace trees
   */
  private void replaceTree() {
    UIManager.put("TreeUI", MTTreeUI.class.getName());
    UIManager.getDefaults().put(MTTreeUI.class.getName(), MTTreeUI.class);

    UIManager.put("List.sourceListSelectionBackgroundPainter", new MTSelectedTreePainter());
    UIManager.put("List.sourceListFocusedSelectionBackgroundPainter", new MTSelectedTreePainter());

  }
}