package com.chrisrm.idea.themes;

import com.chrisrm.idea.MTConfig;
import com.chrisrm.idea.config.BeforeConfigNotifier;
import com.chrisrm.idea.config.ConfigNotifier;
import com.chrisrm.idea.config.ui.MTForm;
import com.chrisrm.idea.messages.MaterialThemeBundle;
import com.chrisrm.idea.ui.*;
import com.chrisrm.idea.utils.UIReplacer;
import com.intellij.ide.ui.LafManager;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.impl.ApplicationImpl;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.messages.MessageBusConnection;
import javassist.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class MTLafComponent extends JBPanel implements ApplicationComponent {

  private boolean willRestartIde = false;

  public MTLafComponent(LafManager lafManager) {
    lafManager.addLafManagerListener(source -> installMaterialComponents());
  }

  @Override
  public void initComponent() {
    installMaterialComponents();

    // Patch UI
    UIReplacer.patchUI();

    // Listen for changes on the settings
    MessageBusConnection connect = ApplicationManager.getApplication().getMessageBus().connect();
    connect.subscribe(ConfigNotifier.CONFIG_TOPIC, this::onSettingsChanged);
    connect.subscribe(BeforeConfigNotifier.BEFORE_CONFIG_TOPIC, (this::onBeforeSettingsChanged));
  }

  @Override
  public void disposeComponent() {

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
  private void onBeforeSettingsChanged(MTConfig mtConfig, MTForm form) {
    // Force restart if material design is switched
    restartIdeIfNecessary(mtConfig, form);
  }

  /**
   * Called when MT Config settings are changeds
   *
   * @param mtConfig
   */
  private void onSettingsChanged(MTConfig mtConfig) {
    // Trigger file icons and statuses update
    ApplicationManager.getApplication().runWriteAction(() -> {
      FileTypeManagerEx instanceEx = FileTypeManagerEx.getInstanceEx();
      instanceEx.fireFileTypesChanged();
      ActionToolbarImpl.updateAllToolbarsImmediately();
    });

    if (this.willRestartIde) {
      this.restartIde();
    }
  }

  /**
   * Restart IDE if necessary (ex: material design components)
   *
   * @param mtConfig
   * @param form
   */
  private void restartIdeIfNecessary(MTConfig mtConfig, MTForm form) {
    // Restart the IDE if changed
    if (mtConfig.needsRestart(form)) {
      String title = MaterialThemeBundle.message("mt.restartDialog.title");
      String message = MaterialThemeBundle.message("mt.restartDialog.content");

      int answer = Messages.showYesNoDialog(message, title, Messages.getQuestionIcon());
      if (answer == Messages.YES) {
        this.willRestartIde = true;
      }
    }
  }

  private void restartIde() {
    Application application = ApplicationManager.getApplication();
    if (application instanceof ApplicationImpl) {

      ((ApplicationImpl) application).restart(true);
    }
    else {
      application.restart();
    }
  }

  /**
   * Hack SearchTextField to override SDK's createUI
   */
  private static void hackSearchTextField() throws NotFoundException, CannotCompileException,
      IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
    ClassPool cp = ClassPool.getDefault();
    cp.insertClassPath(new ClassClassPath(MTTextFieldUI.class));

    CtClass darculaClass = cp.get("com.intellij.ide.ui.laf.darcula.ui.DarculaTextFieldUI");
    CtClass componentClass = cp.get("javax.swing.JComponent");
    CtMethod createUI = darculaClass.getDeclaredMethod("createUI", new CtClass[] {componentClass});
    createUI.setBody("{ return com.chrisrm.idea.ui.MTTextFieldFactory.newInstance($1); }");
    darculaClass.toClass();
  }

  private void replaceTableHeaders() {
    UIManager.put("TableHeaderUI", MTTableHeaderUI.class.getName());
    UIManager.getDefaults().put(MTTableHeaderUI.class.getName(), MTTableHeaderUI.class);

    UIManager.put("TableHeader.border", new MTTableHeaderBorder());
  }

  /**
   * Replace progress bar (TODO: Material Progress bar)
   */
  private void replaceProgressBar() {
    UIManager.put("ProgressBarUI", MTProgressBarUI.class.getName());
    UIManager.getDefaults().put(MTProgressBarUI.class.getName(), MTProgressBarUI.class);

    UIManager.put("ProgressBar.border", new MTProgressBarBorder());
  }

  /**
   * Replace text fields (TODO: replace password fields)
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
    MTConfig mtConfig = MTConfig.getInstance();

    if (mtConfig.getIsMaterialDesign()) {
      replaceButtons();
      replaceTextFields();
      replaceProgressBar();
      replaceTree();
      replaceTableHeaders();
    }
  }

  private void replaceTree() {
    UIManager.put("TreeUI", MTTreeUI.class.getName());
    UIManager.getDefaults().put(MTTreeUI.class.getName(), MTTreeUI.class);
  }
}