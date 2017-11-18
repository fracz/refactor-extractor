/*
 * Copyright 2000-2007 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.openapi.ui;

import com.intellij.CommonBundle;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.MnemonicHelper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.StackingPopupDispatcher;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.UIBundle;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.AwtVisitor;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The dialog wrapper. The dialog wrapper could be used only on event dispatch thread.
 * In case when the dialog must be created from other threads use
 * {@link EventQueue#invokeLater(Runnable)} or {@link EventQueue#invokeAndWait(Runnable)}.
 *
 */
public abstract class DialogWrapper {
  /**
   * The default exit code for "OK" action.
   */
  public static final int OK_EXIT_CODE = 0;
  /**
   * The default exit code for "Cancel" action.
   */
  public static final int CANCEL_EXIT_CODE = 1;
  /**
   * If you use your custom exit codes you have have to start them with
   * this constant.
   */
  public static final int NEXT_USER_EXIT_CODE = 2;

  /**
   * If your action returned by <code>createActions</code> method has non
   * <code>null</code> value for this key, then the button that corresponds to the action will be the
   * default button for the dialog. It's true if you don't change this behaviour
   * of <code>createJButtonForAction(Action)</code> method.
   */
  @NonNls public static final String DEFAULT_ACTION = "DefaultAction";

  private final DialogWrapperPeer myPeer;
  private int myExitCode = CANCEL_EXIT_CODE;

  /**
   * The shared instance of default border for dialog's content pane.
   */
  public static final Border ourDefaultBorder = BorderFactory.createEmptyBorder(8, 8, 8, 8);

  private float myHorizontalStretch = 1.0f;
  private float myVerticalStretch = 1.0f;
  /**
   * Defines horizontal alignment of buttons.
   */
  private int myButtonAlignment = SwingConstants.RIGHT;
  private boolean myCrossClosesWindow = true;
  private Insets myButtonMargins = new Insets(2, 16, 2, 16);

  private Action myOKAction;
  private Action myCancelAction;
  private Action myHelpAction;
  private Component[] myButtons;

  private boolean myClosed = false;

  protected boolean myPerformAction = false;

  private Action myYesAction = null;
  private Action myNoAction = null;

  protected final Disposable myDisposable = new Disposable() {
    public String toString() {
      return DialogWrapper.this.toString();
    }

    public void dispose() {
      DialogWrapper.this.dispose();
    }
  };
  private ErrorText myErrorText;


  /**
   * Creates modal <code>DialogWrapper</code>. The currently active window will be the dialog's parent.
   *
   * @param project     parent window for the dialog will be calculated based on focused window for the
   *                    specified <code>project</code>. This parameter can be <code>null</code>. In this case parent window
   *                    will be suggested based on current focused window.
   * @param canBeParent specifies whether the dialog can be parent for other windows. This parameter is used
   *                    by <code>WindowManager</code>.
   * @throws IllegalStateException if the dialog is invoked not on the event dispatch thread
   */
  protected DialogWrapper(Project project, boolean canBeParent) {
    ensureEventDispatchThread();
    myPeer = DialogWrapperPeerFactory.getInstance().createPeer(this, project, canBeParent);
    createDefaultActions();
  }

  /**
   * Creates modal <code>DialogWrapper</code>. The currently active window will be the dialog's parent.
   *
   * @param canBeParent specifies whether the dialog can be parent for other windows. This parameter is used
   *                    by <code>WindowManager</code>.
   * @throws IllegalStateException if the dialog is invoked not on the event dispatch thread
   */
  protected DialogWrapper(boolean canBeParent) {
    this((Project)null, canBeParent);
  }

  protected DialogWrapper(boolean canBeParent, boolean toolkitModalIfPossible) {
    ensureEventDispatchThread();
    myPeer = DialogWrapperPeerFactory.getInstance().createPeer(this, canBeParent, toolkitModalIfPossible);
    createDefaultActions();
  }

  /**
   * @param parent parent component whicg is used to canculate heavy weight window ancestor.
   *               <code>parent</code> cannot be <code>null</code> and must be showing.
   * @throws IllegalStateException if the dialog is invoked not on the event dispatch thread
   */
  protected DialogWrapper(Component parent, boolean canBeParent) {
    ensureEventDispatchThread();
    myPeer = DialogWrapperPeerFactory.getInstance().createPeer(this, parent, canBeParent);
    createDefaultActions();
  }

  protected void createDefaultActions() {
    myOKAction = new OkAction();
    myCancelAction = new CancelAction();
    myHelpAction = new HelpAction();
  }

  public void setUndecorated(boolean undecorated) {
    myPeer.setUndecorated(undecorated);
  }

  /**
   * @see java.awt.Component#addMouseListener
   */
  public final void addMouseListener(MouseListener listener) {
    myPeer.addMouseListener(listener);
  }

  /**
   * @see java.awt.Component#addMouseMotionListener
   */
  public final void addMouseListener(MouseMotionListener listener) {
    myPeer.addMouseListener(listener);
  }

  /**
   * @see java.awt.Component#addKeyListener
   */
  public final void addKeyListener(KeyListener listener) {
    myPeer.addKeyListener(listener);
  }

  /**
   * Closes and disposes the dialog and sets the specified exit code.
   *
   * @throws IllegalStateException if the dialog is invoked not on the event dispatch thread
   */
  public final void close(int exitCode) {
    ensureEventDispatchThread();
    if (myClosed) return;
    myClosed = true;
    myExitCode = exitCode;
    Disposer.dispose(myDisposable);
  }

  /**
   * Factory method. It creates border for dialog's content pane. By default content
   * pane has has empty border with <code>(8,8,8,8)</code> insets. The subclasses can
   * retirn <code>null</code> in overridden methods. In this case there will be no
   * any border in the content pane.
   */
  @Nullable
  protected Border createContentPaneBorder() {
    return ourDefaultBorder;
  }

  /**
   * This is factory method. It creates the panel located at the south of the content pane. By default that
   * panel contains dialog's buttons. This default implementation uses <code>createActions()</code>
   * and <code>createJButtonForAction(Action)</code> methods to construct the panel.
   */
  @Nullable
  protected JComponent createSouthPanel() {
    Action[] actions = createActions();
    Action[] leftSideActions = createLeftSideActions();
    ArrayList<Component> buttons = new ArrayList<Component>();

    boolean hasHelpToMoveToLeftSide = false;
    if (SystemInfo.isMacOSLeopard && Arrays.asList(actions).contains(getHelpAction())) {
      hasHelpToMoveToLeftSide = true;
      actions = ArrayUtil.remove(actions, getHelpAction());
    }

    JPanel panel = new JPanel(new BorderLayout());
    final JPanel lrButtonsPanel = new JPanel(new GridBagLayout());
    final Insets insets = SystemInfo.isMacOSLeopard ? new Insets(0, 0, 0, 0) : new Insets(8, 0, 0, 0);

    if (actions.length > 0 || leftSideActions.length > 0) {
      int gridx = 0;
      if (leftSideActions.length > 0) {
        JPanel buttonsPanel = createButtons(leftSideActions, buttons);
        lrButtonsPanel.add(buttonsPanel,
                           new GridBagConstraints(gridx++, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0,
                                                  0));

      }
      lrButtonsPanel.add(// left strut
                         Box.createHorizontalGlue(),
                         new GridBagConstraints(gridx++, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0,
                                                0));
      if (actions.length > 0) {
        JPanel buttonsPanel = createButtons(actions, buttons);
        lrButtonsPanel.add(buttonsPanel,
                           new GridBagConstraints(gridx++, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0,
                                                  0));
      }
      if (SwingConstants.CENTER == myButtonAlignment) {
        lrButtonsPanel.add(// right strut
                           Box.createHorizontalGlue(),
                           new GridBagConstraints(gridx, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0,
                                                  0));
      }
      myButtons = buttons.toArray(new Component[buttons.size()]);
    }

    if (hasHelpToMoveToLeftSide) {
      JButton helpButton = new JButton(getHelpAction());
      helpButton.putClientProperty("JButton.buttonType", "help");
      helpButton.setText("");
      helpButton.setMargin(insets);
      panel.add(helpButton, BorderLayout.WEST);
    }

    panel.add(lrButtonsPanel, BorderLayout.CENTER);
    panel.setBorder(IdeBorderFactory.createEmptyBorder(new Insets(8, 0, 0, 0)));
    return panel;
  }

  private JPanel createButtons(Action[] actions, ArrayList<Component> buttons) {
    JPanel buttonsPanel = new JPanel(new GridLayout(1, actions.length, SystemInfo.isMacOSLeopard ? 0 : 5, 0));
    for (final Action action : actions) {
      JButton button = createJButtonForAction(action);
      final Object value = action.getValue(Action.MNEMONIC_KEY);
      if (value instanceof Integer) {
        final int mnemonic = ((Integer)value).intValue();
        if (mnemonic == 'Y') {
          myYesAction = action;
        }
        else if (mnemonic == 'N') {
          myNoAction = action;
        }
        button.setMnemonic(mnemonic);
      }

      buttons.add(button);
      buttonsPanel.add(button);
    }
    return buttonsPanel;
  }

  /**
   * Creates <code>JButton</code> for the specified action. If the button has not <code>null</code>
   * value for <code>DialogWrapper.DEFAULT_ACTION</code> key then the created button will be the
   * default one for the dialog.
   *
   * @see com.intellij.openapi.ui.DialogWrapper#DEFAULT_ACTION
   */
  protected JButton createJButtonForAction(Action action) {
    JButton button = new JButton(action);
    String text = button.getText();

    if (SystemInfo.isMac) {
      button.putClientProperty("JButton.buttonType", "text");
    }

    if (text != null) {
      int mnemonic = 0;
      StringBuilder plainText = new StringBuilder();
      for (int i = 0; i < text.length(); i++) {
        char ch = text.charAt(i);
        if (ch == '_' || ch == '&') {
          i++;
          if (i >= text.length()) {
            break;
          }
          ch = text.charAt(i);
          if (ch != '_' && ch != '&') {
            // Mnemonic is case insensitive.
            int vk = ch;
            if (vk >= 'a' && vk <= 'z') {
              vk -= 'a' - 'A';
            }
            mnemonic = vk;
          }
        }
        plainText.append(ch);
      }
      button.setText(plainText.toString());

      if (mnemonic == KeyEvent.VK_Y) {
        myYesAction = action;
      }
      else if (mnemonic == KeyEvent.VK_N) {
        myNoAction = action;
      }

      button.setMnemonic(mnemonic);
    }
    setMargin(button);
    if (action.getValue(DEFAULT_ACTION) != null) {
      getRootPane().setDefaultButton(button);
    }
    return button;
  }

  private void setMargin(JButton button) {
    // Aqua LnF does a good job of setting proper margin between buttons. Setting them specifically causes them be 'square' style instead of
    // 'rounded', which is expected by apple users.
    if (!SystemInfo.isMac) {
      if (myButtonMargins == null) {
        return;
      }
      button.setMargin(myButtonMargins);
    }
  }


  protected JComponent createTitlePane() {
    return null;
  }

  /**
   * Factory method. It creates the panel located at the
   * north of the dialog's content pane. The implementation can return <code>null</code>
   * value. In this case there will be no input panel.
   */
  protected JComponent createNorthPanel() {
    return null;
  }

  /**
   * Factory method. It creates panel with dialog options. Options panel is located at the
   * center of the dialog's content pane. The implementation can return <code>null</code>
   * value. In this case there will be no options panel.
   */
  @Nullable
  protected abstract JComponent createCenterPanel();

  /**
   * @see java.awt.Window#toFront()
   */
  public void toFront() {
    myPeer.toFront();
  }

  /**
   * @see java.awt.Window#toBack()
   */
  public void toBack() {
    myPeer.toBack();
  }

  /**
   * Dispose the wrapped and releases all resources allocated be the wrapper to help
   * more effecient garbage collection. You should never invoke this method twice or
   * invoke any method of the wrapper after invocation of <code>dispose</code>.
   *
   * @throws IllegalStateException if the dialog is disposed not on the event dispatch thread
   */
  protected void dispose() {
    ensureEventDispatchThread();
    final JRootPane rootPane = getRootPane();
    // if rootPane = null, dialog has already been disposed
    if (rootPane != null) {
      new AwtVisitor(rootPane) {
        public boolean visit(final Component component) {
          if (component instanceof JComponent) {
            final JComponent eachComp = (JComponent)component;
            final KeyStroke[] strokes = eachComp.getRegisteredKeyStrokes();
            for (KeyStroke eachStroke : strokes) {
              eachComp.unregisterKeyboardAction(eachStroke);
            }
          }
          return false;
        }
      };
      myPeer.dispose();
    }
  }


  /**
   * This method is invoked by default implementation of "Cancel" action. It just closes dialog
   * with <code>CANCEL_EXIT_CODE</code>. This is convenient place to override functionality of "Cancel" action.
   * Note that the method does nothing if "Cancel" action isn't enabled.
   */
  public void doCancelAction() {
    if (getCancelAction().isEnabled()) {
      close(CANCEL_EXIT_CODE);
    }
  }

  /**
   * Programmatically perform a "click" of default dialog's button. The method does
   * nothing if the dialog has no default button.
   */
  public void clickDefaultButton() {
    JButton button = getRootPane().getDefaultButton();
    if (button != null) {
      button.doClick();
    }
  }

  /**
   * This method is invoked by default implementation of "OK" action. It just closes dialog
   * with <code>OK_EXIT_CODE</code>. This is convenient place to override functionality of "OK" action.
   * Note that the method does nothing if "OK" action isn't enabled.
   */
  protected void doOKAction() {
    if (getOKAction().isEnabled()) {
      close(OK_EXIT_CODE);
    }
  }

  /**
   * @return whether the native window cross butoon closes the window or not.
   *         <code>true</code> means that cross performs hide or dispose of the dialog.
   */
  public boolean shouldCloseOnCross() {
    return myCrossClosesWindow;
  }

  /**
   * This is factory method which creates action of dialog. Each action is represented
   * by <code>JButton</code> which is created by <code>createJButtonForAction(Action)</code>
   * method. These buttons are places into panel which is created by <code>createButtonsPanel</code>
   * method. Therefore you have anough ways to customise the dialog by ovverriding of
   * <code>createActions()</code>, <code>createButtonsPanel()</code> and
   * </code>createJButtonForAction(Action)</code> methods. By default the <code>createActions()</code>
   * method returns "OK" and "Cancel" action.
   *
   * @see #createSouthPanel
   * @see #createJButtonForAction
   */
  protected Action[] createActions() {
    return new Action[]{getOKAction(), getCancelAction()};
  }

  protected Action[] createLeftSideActions() {
    return new Action[0];
  }

  /**
   * @return default implementation of "OK" action. This action just invokes
   *         <code>doOKAction()</code> method.
   * @see #doOKAction
   */
  protected Action getOKAction() {
    return myOKAction;
  }

  /**
   * @return default implementation of "Cancel" action. This action just invokes
   *         <code>doCancelAction()</code> method.
   * @see #doCancelAction
   */
  protected Action getCancelAction() {
    return myCancelAction;
  }

  /**
   * @return default implementation of "Help" action. This action just invokes
   *         <code>doHelpAction()</code> method.
   * @see #doHelpAction
   */
  protected Action getHelpAction() {
    return myHelpAction;
  }

  protected boolean isProgressDialog() {
    return false;
  }

  public final boolean isModalProgress() {
    return isProgressDialog();
  }

  /**
   * @see javax.swing.JDialog#getContentPane
   */
  public Container getContentPane() {
    return myPeer.getContentPane();
  }

  /**
   * @see javax.swing.JDialog#validate
   */
  public void validate() {
    myPeer.validate();
  }

  /**
   * @see javax.swing.JDialog#repaint
   */
  public void repaint() {
    myPeer.repaint();
  }

  /**
   * This is factory method. It returns key for installation into the dimension service.
   * If this method returns <code>null</code> then the component does not require installation
   * into dimension service. This default implementation returns <code>null</code>.
   */
  @NonNls
  protected String getDimensionServiceKey() {
    return null;
  }

  public final String getDimensionKey() {
    return getDimensionServiceKey();
  }

  public int getExitCode() {
    return myExitCode;
  }

  /**
   * @return component which should be focused when the dialog appears
   *         on the screen.
   */
  public JComponent getPreferredFocusedComponent() {
    return null;
  }

  /**
   * @return horizontal stretch of the dialog. It means that the dialog's horizontal size is
   *         the product of horizontal stretch by horizontal size of packed dialog. The default value
   *         is <code>1.0f</code>
   */
  public final float getHorizontalStretch() {
    return myHorizontalStretch;
  }

  /**
   * @return vertical stretch of the dialog. It means that the dialog's vertical size is
   *         the product of vertical stretch by vertical size of packed dialog. The default value
   *         is <code>1.0f</code>
   */
  public final float getVerticalStretch() {
    return myVerticalStretch;
  }

  protected final void setHorizontalStretch(float hStretch) {
    myHorizontalStretch = hStretch;
  }

  protected final void setVerticalStretch(float vStretch) {
    myVerticalStretch = vStretch;
  }

  /**
   * @see java.awt.Window#getOwner
   */
  public Window getOwner() {
    return myPeer.getOwner();
  }

  public Window getWindow() {
    return myPeer.getWindow();
  }

  /**
   * @see javax.swing.JDialog#getRootPane
   */
  public JRootPane getRootPane() {
    return myPeer.getRootPane();
  }

  /**
   * @see java.awt.Window#getSize
   */
  public Dimension getSize() {
    return myPeer.getSize();
  }

  /**
   * @see java.awt.Dialog#getTitle
   */
  public String getTitle() {
    return myPeer.getTitle();
  }

  protected void init() {
    myErrorText = new ErrorText();

    JComponent titlePane = createTitlePane();
    JComponent contentPane = createContentPane();
    if (titlePane != null) {
      JPanel newContent = new JPanel(new BorderLayout());
      newContent.add(titlePane, BorderLayout.NORTH);
      newContent.add(contentPane, BorderLayout.CENTER);
      myPeer.setContentPane(newContent);
    }
    else {
      myPeer.setContentPane(contentPane);
    }
    contentPane.setLayout(new BorderLayout());
    Border contentPaneBorder = createContentPaneBorder();
//    if (contentPaneBorder != null) {
    contentPane.setBorder(contentPaneBorder);
//    }
    JComponent northPanel = createNorthPanel();
    if (northPanel != null) {
      contentPane.add(northPanel, BorderLayout.NORTH);
    }
    JComponent centerPanel = createCenterPanel();
    if (centerPanel != null) {
      contentPane.add(centerPanel, BorderLayout.CENTER);
    }
    JComponent southPanel = createSouthPanel();

    final NonOpaquePanel southWrapper = new NonOpaquePanel(new BorderLayout());
    southWrapper.add(myErrorText, BorderLayout.CENTER);

    if (southPanel != null) {
      southWrapper.add(southPanel, BorderLayout.SOUTH);
    }

    contentPane.add(southWrapper, BorderLayout.SOUTH);

    new MnemonicHelper().register(contentPane);
  }

  protected JComponent createContentPane() {
    return new JPanel();
  }

  /**
   * @see java.awt.Window#pack
   */
  public void pack() {
    myPeer.pack();
  }

  public Dimension getPreferredSize() {
    return myPeer.getPreferredSize();
  }

  /**
   * Sets horizontal alignment of dialog's the buttons.
   *
   * @param alignment alignment of the buttons. Acceptable values are
   *                  <code>SwingConstants.CENTER</code> and <code>SwingConstants.RIGHT</code>.
   *                  The <code>SwingConstants.RIGHT</code> is the default value.
   * @throws java.lang.IllegalArgumentException
   *          if <code>alignment</code> isn't acceptable
   */
  protected final void setButtonsAlignment(int alignment) {
    if (SwingConstants.CENTER != alignment && SwingConstants.RIGHT != alignment) {
      throw new IllegalArgumentException("unknown alignment: " + alignment);
    }
    myButtonAlignment = alignment;
  }

  /**
   * Sets margine for command buttons ("OK", "Cance", "Help").
   */
  public final void setButtonsMargin(Insets insets) {
    myButtonMargins = insets;
  }

  public final void setCrossClosesWindow(boolean crossClosesWindow) {
    myCrossClosesWindow = crossClosesWindow;
  }

  protected final void setCancelButtonIcon(Icon icon) {
    // Setting icons causes buttons be 'square' style instead of
    // 'rounded', which is expected by apple users.
    if (!SystemInfo.isMac) {
      myCancelAction.putValue(Action.SMALL_ICON, icon);
    }
  }

  protected final void setCancelButtonText(String text) {
    myCancelAction.putValue(Action.NAME, text);
  }

  public void setModal(boolean modal) {
    myPeer.setModal(modal);
  }

  protected void setOKActionEnabled(boolean isEnabled) {
    myOKAction.setEnabled(isEnabled);
  }

  protected final void setOKButtonIcon(Icon icon) {
    // Setting icons causes buttons be 'square' style instead of
    // 'rounded', which is expected by apple users.
    if (!SystemInfo.isMac) {
      myOKAction.putValue(Action.SMALL_ICON, icon);
    }
  }

  protected final void setOKButtonText(String text) {
    myOKAction.putValue(Action.NAME, text);
  }

  /**
   * This method is invoked by default implementation of "Help" action.
   * This is convenient place to override functionality of "Help" action.
   * Note that the method does nothing if "Help" action isn't enabled.
   */
  protected void doHelpAction() {
    if (myHelpAction.isEnabled()) {
      Messages.showMessageDialog(getContentPane(), UIBundle.message("there.is.no.help.for.this.dialog.error.message"),
                                 UIBundle.message("no.help.available.dialog.title"), Messages.getInformationIcon());
    }
  }

  public boolean isOK() {
    return myExitCode == OK_EXIT_CODE;
  }

  public boolean isOKActionEnabled() {
    return myOKAction.isEnabled();
  }

  /**
   * @see java.awt.Component#isVisible
   */
  public boolean isVisible() {
    return myPeer.isVisible();
  }

  /**
   * @see java.awt.Window#isShowing
   */
  public boolean isShowing() {
    return myPeer.isShowing();
  }

  /**
   * @see javax.swing.JDialog#setSize
   */
  public void setSize(int width, int height) {
    myPeer.setSize(width, height);
  }

  /**
   * @see javax.swing.JDialog#setTitle
   */
  public void setTitle(String title) {
    myPeer.setTitle(title);
  }

  /**
   * @see javax.swing.JDialog#isResizable
   */
  public void isResizable() {
    myPeer.isResizable();
  }

  /**
   * @see javax.swing.JDialog#setResizable
   */
  public void setResizable(boolean resizable) {
    myPeer.setResizable(resizable);
  }

  /**
   * @see javax.swing.JDialog#getLocation
   */
  public Point getLocation() {
    return myPeer.getLocation();
  }

  /**
   * @see javax.swing.JDialog#setLocation(Point)
   */
  public void setLocation(Point p) {
    myPeer.setLocation(p);
  }

  /**
   * @see javax.swing.JDialog#setLocation(int,int)
   */
  public void setLocation(int x, int y) {
    myPeer.setLocation(x, y);
  }

  public void centerRelativeToParent() {
    myPeer.centerInParent();
  }

  /**
   * Show the dialog
   *
   * @throws IllegalStateException if the dialog is invoked not on the event dispatch thread
   */
  public void show() {
    ensureEventDispatchThread();
    registerKeyboardShortcuts();
    myPeer.show();
  }

  /**
   * @return Location in absolute coordinates which is used when dialog has no dimension service key or no position was stored yet.
   *         Can return null. In that case dialog will be centered relative to its owner.
   */
  @Nullable
  public Point getInitialLocation() {
    return null;
  }

  private void registerKeyboardShortcuts() {
    getRootPane().registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MenuSelectionManager menuSelectionManager = MenuSelectionManager.defaultManager();
        MenuElement[] selectedPath = menuSelectionManager.getSelectedPath();
        if (selectedPath.length > 0) { // hide popup menu if any
          menuSelectionManager.clearSelectedPath();
        }
        else if (ApplicationManager.getApplication() == null || !StackingPopupDispatcher.getInstance().isPopupFocused()) {
          doCancelAction();
        }
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

    getRootPane().registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doHelpAction();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

    getRootPane().registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doHelpAction();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_HELP, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

    if (myButtons != null) {
      getRootPane().registerKeyboardAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          focusPreviousButton();
        }
      }, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      getRootPane().registerKeyboardAction(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          focusNextButton();
        }
      }, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    if (myYesAction != null) {
      getRootPane().registerKeyboardAction(myYesAction, KeyStroke.getKeyStroke(KeyEvent.VK_Y, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    if (myNoAction != null) {
      getRootPane().registerKeyboardAction(myNoAction, KeyStroke.getKeyStroke(KeyEvent.VK_N, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
  }

  private void focusPreviousButton() {
    for (int i = 0; i < myButtons.length; i++) {
      if (myButtons[i].hasFocus()) {
        if (i == 0) {
          myButtons[myButtons.length - 1].requestFocus();
          return;
        }
        myButtons[i - 1].requestFocus();
        return;
      }
    }
  }

  private void focusNextButton() {
    for (int i = 0; i < myButtons.length; i++) {
      if (myButtons[i].hasFocus()) {
        if (i == myButtons.length - 1) {
          myButtons[0].requestFocus();
          return;
        }
        myButtons[i + 1].requestFocus();
        return;
      }
    }
  }

  private class OkAction extends AbstractAction {
    public OkAction() {
      putValue(NAME, CommonBundle.getOkButtonText());
      putValue(DEFAULT_ACTION, Boolean.TRUE);
    }

    public void actionPerformed(ActionEvent e) {
      if (myClosed) return;
      if (myPerformAction) return;
      try {
        myPerformAction = true;
        doOKAction();
      }
      finally {
        myPerformAction = false;
      }
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      putValue(NAME, CommonBundle.getCancelButtonText());
    }

    public void actionPerformed(ActionEvent e) {
      if (myClosed) return;
      if (myPerformAction) return;
      try {
        myPerformAction = true;
        doCancelAction();
      }
      finally {
        myPerformAction = false;
      }
    }
  }

  private class HelpAction extends AbstractAction {
    public HelpAction() {
      putValue(NAME, CommonBundle.getHelpButtonText());
    }

    public void actionPerformed(ActionEvent e) {
      doHelpAction();
    }
  }

  protected final void setErrorText(@Nullable String text) {
    myErrorText.setError(text);
    updateHeightForErrorText();
    myErrorText.repaint();
  }

  private void updateHeightForErrorText() {
    if (getRootPane() == null) return;

    int minHeight = getContentPane().getMinimumSize().height;
    if (getContentPane().getHeight() < minHeight) {
      myPeer.setSize(myPeer.getSize().width, minHeight);
    }
  }

  private static class ErrorText extends JPanel {
    private final JLabel myLabel = new JLabel();
    private Dimension myPrefSize;

    public ErrorText() {
      setLayout(new BorderLayout());
      setBorder(null);
      UIUtil.removeQuaquaVisualMarginsIn(this);
      add(myLabel, BorderLayout.CENTER);
    }

    public void setError(String text) {
      if (text == null) {
        myLabel.setText("");
        myLabel.setIcon(null);
        setBorder(null);
      }
      else {
        myLabel.setText("<html><body><font color=red><left>" + text + "</left></b></font></body></html>");
        myLabel.setIcon(IconLoader.getIcon("/actions/lightning.png"));
        myLabel.setBorder(new EmptyBorder(2, 2, 0, 0));
        if (myPrefSize == null) {
          myPrefSize = myLabel.getPreferredSize();
        }
      }
      revalidate();
    }

    public Dimension getPreferredSize() {
      return myPrefSize == null ? super.getPreferredSize() : myPrefSize;
    }
  }

  public final DialogWrapperPeer getPeer() {
    return myPeer;
  }

  /**
   * Ensure that dialog is used from even dispatch thread.
   *
   * @throws IllegalStateException if the dialog is invoked not on the event dispatch thread
   */
  private static void ensureEventDispatchThread() {
    if (!EventQueue.isDispatchThread()) {
      throw new IllegalStateException("The DialogWrapper can be used only on event dispatch thread.");
    }
  }
}