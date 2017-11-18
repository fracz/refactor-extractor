package com.intellij.openapi.ui.impl;

import com.intellij.ide.DataManager;
import com.intellij.ide.impl.TypeSafeDataProviderAdapter;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.impl.LaterInvocator;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.CommandProcessorEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.DialogWrapperDialog;
import com.intellij.openapi.ui.DialogWrapperPeer;
import com.intellij.openapi.ui.popup.StackingPopupDispatcher;
import com.intellij.openapi.util.DimensionService;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.openapi.wm.impl.IdeGlassPaneImpl;
import com.intellij.ui.FocusTrackback;
import com.intellij.ui.ScreenUtil;
import com.intellij.ui.SpeedSearchBase;
import com.intellij.ui.popup.StackingPopupDispatcherImpl;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class DialogWrapperPeerImpl extends DialogWrapperPeer {
  public static Object HAVE_INITIAL_SELECTION = new Object();

  private static final Logger LOG = Logger.getInstance("#com.intellij.openapi.ui.DialogWrapper");

  private DialogWrapper myWrapper;
  private MyDialog myDialog;
  private boolean myCanBeParent = true;
  /*
   * Default dialog's actions.
   */
  private WindowManagerEx myWindowManager;
  private java.util.List<Runnable> myDisposeActions = new ArrayList<Runnable>();
  private Project myProject;

  /**
   * Creates modal <code>DialogWrapper</code>. The currently active window will be the dialog's parent.
   *
   * @param project     parent window for the dialog will be calculated based on focused window for the
   *                    specified <code>project</code>. This parameter can be <code>null</code>. In this case parent window
   *                    will be suggested based on current focused window.
   * @param canBeParent specifies whether the dialog can be parent for other windows. This parameter is used
   *                    by <code>WindowManager</code>.
   */
  protected DialogWrapperPeerImpl(DialogWrapper wrapper, Project project, boolean canBeParent) {
    myWrapper = wrapper;
    myWindowManager = null;
    Application application = ApplicationManager.getApplication();
    if (application != null && application.hasComponent(WindowManager.class)) {
      myWindowManager = (WindowManagerEx)WindowManager.getInstance();
    }

    Window window = null;
    if (myWindowManager != null) {

      if (project == null) {
        project = PlatformDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext());
      }

      myProject = project;

      window = myWindowManager.suggestParentWindow(project);
      if (window == null) {
        Window focusedWindow = myWindowManager.getMostRecentFocusedWindow();
        if (focusedWindow instanceof IdeFrameImpl) {
          window = focusedWindow;
        }
      }
    }

    Window owner;
    if (window != null) {
      owner = window;
    }
    else {
      owner = JOptionPane.getRootFrame();
    }

    createDialog(owner, canBeParent);
  }

  protected DialogWrapperPeerImpl(DialogWrapper wrapper, boolean canBeParent) {
    this(wrapper, (Project)null, canBeParent);
  }

  /**
   * @param parent parent component whicg is used to canculate heavy weight window ancestor.
   *               <code>parent</code> cannot be <code>null</code> and must be showing.
   */
  protected DialogWrapperPeerImpl(DialogWrapper wrapper, @NotNull Component parent, boolean canBeParent) {
    myWrapper = wrapper;
    if (!parent.isShowing() && parent != JOptionPane.getRootFrame()) {
      throw new IllegalArgumentException("parent must be showing: " + parent);
    }
    myWindowManager = null;
    Application application = ApplicationManager.getApplication();
    if (application != null && application.hasComponent(WindowManager.class)) {
      myWindowManager = (WindowManagerEx)WindowManager.getInstance();
    }

    Window owner = parent instanceof Window ? (Window)parent : (Window)SwingUtilities.getAncestorOfClass(Window.class, parent);
    if (!(owner instanceof Dialog) && !(owner instanceof Frame)) {
      owner = JOptionPane.getRootFrame();
    }
    createDialog(owner, canBeParent);
  }

  public DialogWrapperPeerImpl(final DialogWrapper wrapper, final boolean canBeParent, final boolean tryToolkitModal) {
    myWrapper = wrapper;
    myWindowManager = null;
    Application application = ApplicationManager.getApplication();
    if (application != null && application.hasComponent(WindowManager.class)) {
      myWindowManager = (WindowManagerEx)WindowManager.getInstance();
    }
    if (UIUtil.hasJdk6Dialogs()) {
      createDialog(null, canBeParent);
      if (tryToolkitModal) {
        UIUtil.setToolkitModal(myDialog);
      }
    }
    else {
      createDialog(JOptionPane.getRootFrame(), canBeParent);
    }
  }

  public void setUndecorated(boolean undecorated) {
    myDialog.setUndecorated(undecorated);
  }

  public void addMouseListener(MouseListener listener) {
    myDialog.addMouseListener(listener);
  }

  public void addMouseListener(MouseMotionListener listener) {
    myDialog.addMouseMotionListener(listener);
  }

  public void addKeyListener(KeyListener listener) {
    myDialog.addKeyListener(listener);
  }

  private void createDialog(Window owner, boolean canBeParent) {
    if (owner instanceof Frame) {
      myDialog = new MyDialog((Frame)owner, myWrapper, myProject);
    }
    else {
      myDialog = new MyDialog((Dialog)owner, myWrapper, myProject);
    }
    myDialog.setModal(true);
    myCanBeParent = canBeParent;

  }


  public void toFront() {
    myDialog.toFront();
  }

  public void toBack() {
    myDialog.toBack();
  }

  protected void dispose() {
    LOG.assertTrue(EventQueue.isDispatchThread(), "Access is allowed from event dispatch thread only");
    for (Runnable runnable : myDisposeActions) {
      runnable.run();
    }
    myDisposeActions.clear();
    myDialog.remove(myDialog.getRootPane());

    Runnable disposer = new Runnable() {
      public void run() {
        myDialog.dispose();
        /*
        if (myWindowManager == null) {
          myDialog.dispose();
        }
        else {
          myWindowManager.hideDialog(myDialog, myProject);
        }
        */
      }
    };

    if (EventQueue.isDispatchThread()) {
      disposer.run();
    }
    else {
      SwingUtilities.invokeLater(disposer);
    }
  }

  private boolean isProgressDialog() {
    return myWrapper.isModalProgress();
  }

  @Nullable
  public Container getContentPane() {
    return getRootPane() != null ? myDialog.getContentPane() : null;
  }

  /**
   * @see javax.swing.JDialog#validate
   */
  public void validate() {
    myDialog.validate();
  }

  /**
   * @see javax.swing.JDialog#repaint
   */
  public void repaint() {
    myDialog.repaint();
  }

  public Window getOwner() {
    return myDialog.getOwner();
  }

  public Window getWindow() {
    return myDialog;
  }

  public JRootPane getRootPane() {
    return myDialog.getRootPane();
  }

  public Dimension getSize() {
    return myDialog.getSize();
  }

  public String getTitle() {
    return myDialog.getTitle();
  }

  /**
   * @see java.awt.Window#pack
   */
  public void pack() {
    myDialog.pack();
  }

  public void setIconImage(final Image image) {
    UIUtil.updateDialogIcon(myDialog, image);
  }

  public Dimension getPreferredSize() {
    return myDialog.getPreferredSize();
  }

  public void setModal(boolean modal) {
    myDialog.setModal(modal);
  }

  public boolean isVisible() {
    return myDialog.isVisible();
  }

  public boolean isShowing() {
    return myDialog.isShowing();
  }

  public void setSize(int width, int height) {
    myDialog.setSize(width, height);
  }

  public void setTitle(String title) {
    myDialog.setTitle(title);
  }

  public void isResizable() {
    myDialog.isResizable();
  }

  public void setResizable(boolean resizable) {
    myDialog.setResizable(resizable);
  }

  public Point getLocation() {
    return myDialog.getLocation();
  }

  public void setLocation(Point p) {
    myDialog.setLocation(p);
  }

  public void setLocation(int x, int y) {
    myDialog.setLocation(x, y);
  }

  public void show() {
    LOG.assertTrue(EventQueue.isDispatchThread(), "Access is allowed from event dispatch thread only");

    final AnCancelAction anCancelAction = new AnCancelAction();
    final JRootPane rootPane = getRootPane();
    anCancelAction.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)), rootPane);
    myDisposeActions.add(new Runnable() {
      public void run() {
        anCancelAction.unregisterCustomShortcutSet(rootPane);
      }
    });

    if (!myCanBeParent && myWindowManager != null) {
      myWindowManager.doNotSuggestAsParent(myDialog);
    }

    final CommandProcessorEx commandProcessor = ApplicationManager.getApplication() != null ? (CommandProcessorEx)CommandProcessor.getInstance() : null;
    final boolean appStarted = commandProcessor != null;

    if (myDialog.isModal() && !isProgressDialog()) {
      if (ApplicationManager.getApplication() != null) {
        if (ApplicationManager.getApplication().getCurrentWriteAction(null) != null) {
          LOG.warn(
            "Showing of a modal dialog inside write-action may be dangerous and resulting in unpredictable behavior! Current modalityState=" + ModalityState.current(), new Exception());
        }
      }
      if (appStarted) {
        commandProcessor.enterModal();
        LaterInvocator.enterModal(myDialog);
      }
    }

    if (appStarted) {
      hidePopupsIfNeeded();
    }

    try {
      myDialog.show();
    }
    finally {
      if (myDialog.isModal() && !isProgressDialog()) {
        if (appStarted) {
          commandProcessor.leaveModal();
          LaterInvocator.leaveModal(myDialog);
        }
      }
    }
  }

//[kirillk] for now it only deals with the TaskWindow under Mac OS X: modal dialogs are shown behind JBPopup
//hopefully this whole code will go away
  private void hidePopupsIfNeeded() {
    if (!SystemInfo.isMac) return;

    StackingPopupDispatcherImpl.getInstance().hidePersistentPopups();
    myDisposeActions.add(new Runnable() {
      public void run() {
        StackingPopupDispatcherImpl.getInstance().restorePersistentPopups();
      }
    });
  }

  public FocusTrackback getFocusTrackback() {
    return myDialog.getFocusTrackback();
  }

  private class AnCancelAction extends AnAction {
    public void update(AnActionEvent e) {
      Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
      e.getPresentation().setEnabled(false);
      if (focusOwner instanceof JComponent && SpeedSearchBase.hasActiveSpeedSearch((JComponent)focusOwner)) {
        return;
      }

      if (StackingPopupDispatcher.getInstance().isPopupFocused()) return;

      if (focusOwner instanceof JTree) {
        JTree tree = (JTree)focusOwner;
        if (!tree.isEditing()) {
          e.getPresentation().setEnabled(true);
        }
      }
      else if (focusOwner instanceof JTable) {
        JTable table = (JTable)focusOwner;
        if (!table.isEditing()) {
          e.getPresentation().setEnabled(true);
        }
      }
    }

    public void actionPerformed(AnActionEvent e) {
      myWrapper.doCancelAction();
    }
  }


  private static class MyDialog extends JDialog implements DialogWrapperDialog, DataProvider, FocusTrackback.Provider {
    private final WeakReference<DialogWrapper> myDialogWrapper;
    /**
     * Initial size of the dialog. When the dialog is being closed and
     * current size of the dialog is not equals to the initial sizethen the
     * current (changed) size is stored in the <code>DimensionService</code>.
     */
    private Dimension myInitialSize;
    private String myDimensionServiceKey;
    private boolean myOpened = false;

    private FocusTrackback myFocusTrackback;
    private MyDialog.MyWindowListener myWindowListener;
    private MyDialog.MyComponentListener myComponentListener;

    private WeakReference<Project> myProject;

    public MyDialog(Dialog owner, DialogWrapper dialogWrapper, Project project) {
      super(owner);
      myDialogWrapper = new WeakReference<DialogWrapper>(dialogWrapper);
      myProject = project != null ? new WeakReference<Project>(project) : null;
      initDialog();
    }

    public MyDialog(Frame owner, DialogWrapper dialogWrapper, Project project) {
      super(owner);
      myDialogWrapper = new WeakReference<DialogWrapper>(dialogWrapper);
      myProject = project != null ? new WeakReference<Project>(project) : null;
      initDialog();
    }

    private void initDialog() {
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      myWindowListener = new MyWindowListener();
      addWindowListener(myWindowListener);
      myComponentListener = new MyComponentListener();
      addComponentListener(myComponentListener);
    }

    public FocusTrackback getFocusTrackback() {
      return myFocusTrackback;
    }

    public DialogWrapper getDialogWrapper() {
      return myDialogWrapper.get();
    }

    public void centerInParent() {
      setLocationRelativeTo(getOwner());
    }

    public Object getData(String dataId) {
      final DialogWrapper wrapper = myDialogWrapper.get();
      if (wrapper instanceof DataProvider) {
        return ((DataProvider)wrapper).getData(dataId);
      }
      else if (wrapper instanceof TypeSafeDataProvider) {
        TypeSafeDataProviderAdapter adapter = new TypeSafeDataProviderAdapter((TypeSafeDataProvider)wrapper);
        return adapter.getData(dataId);
      }
      return null;
    }

    public void setSize(int width, int height) {
      Point location = getLocation();
      Rectangle rect = new Rectangle(location.x, location.y, width, height);
      ScreenUtil.fitToScreen(rect);
      if (location.x != rect.x || location.y != rect.y) {
        setLocation(rect.x, rect.y);
      }

      super.setSize(rect.width, rect.height);
    }

    public void setBounds(int x, int y, int width, int height) {
      Rectangle rect = new Rectangle(x, y, width, height);
      ScreenUtil.fitToScreen(rect);
      super.setBounds(rect.x, rect.y, rect.width, rect.height);
    }

    public void setBounds(Rectangle r) {
      ScreenUtil.fitToScreen(r);
      super.setBounds(r);
    }

    protected JRootPane createRootPane() {
      return new DialogRootPane();
    }

    public void show() {
      myFocusTrackback = new FocusTrackback(myDialogWrapper, this, true);

      final DialogWrapper dialogWrapper = getDialogWrapper();

      pack();
      setSize((int)(getWidth() * dialogWrapper.getHorizontalStretch()), (int)(getHeight() * dialogWrapper.getVerticalStretch()));

      // Restore dialog's size and location

      myDimensionServiceKey = dialogWrapper.getDimensionKey();
      Point location = null;

      if (myDimensionServiceKey != null) {
        final Project projectGuess = PlatformDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(this));
        location = DimensionService.getInstance().getLocation(myDimensionServiceKey, projectGuess);
        Dimension size = DimensionService.getInstance().getSize(myDimensionServiceKey, projectGuess);
        if (size != null) {
          myInitialSize = (Dimension)size.clone();
          setSize(myInitialSize);
        }
      }

      if (myInitialSize == null) {
        myInitialSize = getSize();
      }

      if (location == null) {
        location = dialogWrapper.getInitialLocation();
      }

      if (location != null) {
        setLocation(location);
      }
      else {
        setLocationRelativeTo(getOwner());
      }

      final Rectangle bounds = getBounds();
      ScreenUtil.fitToScreen(bounds);
      setBounds(bounds);

      addWindowListener(new WindowAdapter() {
        public void windowActivated(final WindowEvent e) {
          final DialogWrapper wrapper = getDialogWrapper();
          if (wrapper != null && myFocusTrackback != null) {
            myFocusTrackback.registerFocusComponent(new FocusTrackback.ComponentQuery() {
              public Component getComponent() {
                return wrapper.getPreferredFocusedComponent();
              }
            });
          }
        }

        public void windowDeactivated(final WindowEvent e) {
          if (!isModal()) {
            final Ref<IdeFocusManager> focusManager = new Ref<IdeFocusManager>(null);
            if (myProject != null && myProject.get() != null) {
              focusManager.set(IdeFocusManager.getInstance(myProject.get()));
              focusManager.get().doWhenFocusSettlesDown(new Runnable() {
                public void run() {
                  disposeFocusTrackbackIfNoChildWindowFocused(focusManager.get());
                }
              });
            } else {
              disposeFocusTrackbackIfNoChildWindowFocused(focusManager.get());
            }
          }
        }
      });

      super.show();
    }

    private void disposeFocusTrackbackIfNoChildWindowFocused(@Nullable IdeFocusManager focusManager) {
      if (myFocusTrackback == null) return;

      final DialogWrapper wrapper = myDialogWrapper.get();
      if (wrapper == null || !wrapper.isShowing()) {
        myFocusTrackback.dispose();
        return;
      }

      if (focusManager != null) {
        final Component c = focusManager.getFocusedDescendantFor(wrapper.getContentPane());
        if (c == null) {
          myFocusTrackback.dispose();
        }
      } else {
        final Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (owner == null || !SwingUtilities.isDescendingFrom(owner, wrapper.getContentPane())) {
          myFocusTrackback.dispose();
        }
      }
    }

    @Deprecated
    public void hide() {
      super.hide();
      if (myFocusTrackback != null) {
        myFocusTrackback.restoreFocus();
      }
    }

    public void dispose() {
      if (isShowing()) {
        hide();
      }

      if (myWindowListener != null) {
        myWindowListener.saveSize();
        removeWindowListener(myWindowListener);
        myWindowListener = null;
      }
      if (myComponentListener != null) {
        removeComponentListener(myComponentListener);
        myComponentListener = null;
      }
      if (myFocusTrackback != null) {
        myFocusTrackback.dispose();
        myFocusTrackback = null;
      }
      final BufferStrategy strategy = getBufferStrategy();
      if (strategy != null) {
        try {
          Method method = strategy.getClass().getMethod("dispose");   // added in JDK 1.6 so cannot call directly
          method.invoke(strategy);
        }
        catch (Exception ex) {
          // ignore
        }
      }
      super.dispose();

      if (rootPane != null) { // Workaround for bug in native code to hold rootPane
        try {
          Field field = rootPane.getClass().getDeclaredField("glassPane");
          field.setAccessible(true);
          field.set(rootPane, null);

          field = rootPane.getClass().getDeclaredField("contentPane");
          field.setAccessible(true);
          field.set(rootPane, null);
          rootPane = null;

          field = Window.class.getDeclaredField("windowListener");
          field.setAccessible(true);
          field.set(this, null);
        }
        catch (Exception e) {
        }
      }
    }

    @Override
    public Component getMostRecentFocusOwner() {
      if (!myOpened) {
        final DialogWrapper wrapper = getDialogWrapper();
        if (wrapper != null) {
          JComponent toFocus = wrapper.getPreferredFocusedComponent();
          if (toFocus != null) {
            return toFocus;
          }
        }
      }
      return super.getMostRecentFocusOwner();
    }

    @Override
    public void paint(Graphics g) {
      UIUtil.applyRenderingHints(g);
      super.paint(g);
    }

    private class MyWindowListener extends WindowAdapter {
      public void windowClosing(WindowEvent e) {
        DialogWrapper dialogWrapper = getDialogWrapper();
        if (dialogWrapper.shouldCloseOnCross()) {
          dialogWrapper.doCancelAction();
        }
      }

      public void windowClosed(WindowEvent e) {
        saveSize();
      }

      public void saveSize() {
        if (myDimensionServiceKey != null &&
            myInitialSize != null &&
            myOpened) { // myInitialSize can be null only if dialog is disposed before first showing
          final Project projectGuess = PlatformDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(MyDialog.this));

          // Save location
          Point location = getLocation();
          DimensionService.getInstance().setLocation(myDimensionServiceKey, location, projectGuess);
          // Save size
          Dimension size = getSize();
          if (!myInitialSize.equals(size)) {
            DimensionService.getInstance().setSize(myDimensionServiceKey, size, projectGuess);
          }
          myOpened = false;
        }
      }

      public void windowOpened(WindowEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            myOpened = true;
            final DialogWrapper activeWrapper = getActiveWrapper();
            if (activeWrapper == null) {
              return;
            }

            JComponent toFocus = activeWrapper.getPreferredFocusedComponent();
            if (toFocus == null) {
              toFocus = getRootPane().getDefaultButton();
            }

            moveMousePointerOnButton(getRootPane().getDefaultButton());
            setupSelectionOnPreferredComponent(toFocus);

            if (toFocus != null) {
              final JComponent toRequest = toFocus;
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  toRequest.requestFocus();
                }
              });
            }
          }
        });
      }

      private DialogWrapper getActiveWrapper() {
        DialogWrapper activeWrapper = getDialogWrapper();
        if (activeWrapper == null || !activeWrapper.isShowing()) {
          return null;
        }

        return activeWrapper;
      }

      private void moveMousePointerOnButton(final JButton button) {
        Application application = ApplicationManager.getApplication();
        if (application != null && application.hasComponent(UISettings.class)) {
          if (button != null && UISettings.getInstance().MOVE_MOUSE_ON_DEFAULT_BUTTON) {
            Point p = button.getLocationOnScreen();
            Rectangle r = button.getBounds();
            try {
              Robot robot = new Robot();
              robot.mouseMove(p.x + r.width / 2, p.y + r.height / 2);
            }
            catch (AWTException exc) {
              exc.printStackTrace();
            }
          }
        }
      }
    }

    private class MyComponentListener extends ComponentAdapter {
      @SuppressWarnings({"RefusedBequest"})
      public void componentResized(ComponentEvent e) {
        final JRootPane pane = getRootPane();
        if (pane == null) return;
        final Dimension minSize = pane.getMinimumSize();
        final Dimension size = pane.getSize();
        final Dimension winSize = getSize();
        if (minSize.width > size.width) {
          winSize.width += minSize.width - size.width;
        }
        if (minSize.height > size.height) {
          winSize.height += minSize.height - size.height;
        }

        if (!winSize.equals(getSize())) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              if (isShowing()) {
                setSize(winSize);
              }
            }
          });
        }
      }
    }

    private static class DialogRootPane extends JRootPane {

      private boolean myGlassPaneIsSet;

      private DialogRootPane() {
        setGlassPane(new IdeGlassPaneImpl(this));
        myGlassPaneIsSet = true;
      }

      @Override
      public void setGlassPane(final Component glass) {
        //todo [kirillk] to remove as new options editor is done
        if (myGlassPaneIsSet && "true".equalsIgnoreCase(System.getProperty("new.options.editor"))) {
          LOG.warn("Setting of glass pane for DialogWrapper is prohibited", new Exception());
          return;
        }

        super.setGlassPane(glass);
      }
    }
  }

  private static void setupSelectionOnPreferredComponent(final JComponent component) {
    if (component instanceof JTextField) {
      JTextField field = (JTextField)component;
      String text = field.getText();
      if (text != null && field.getClientProperty(HAVE_INITIAL_SELECTION) == null) {
        field.setSelectionStart(0);
        field.setSelectionEnd(text.length());
      }
    }
    else if (component instanceof JComboBox) {
      JComboBox combobox = (JComboBox)component;
      combobox.getEditor().selectAll();
    }
  }

  public void setContentPane(JComponent content) {
    myDialog.setContentPane(content);
  }

  public void centerInParent() {
    myDialog.centerInParent();
  }
}