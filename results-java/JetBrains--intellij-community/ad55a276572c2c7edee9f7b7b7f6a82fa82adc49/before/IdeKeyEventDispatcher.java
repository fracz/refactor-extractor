package com.intellij.openapi.keymap.impl;

import com.intellij.ide.DataManager;
import com.intellij.ide.IdeEventQueue;
import com.intellij.ide.impl.DataManagerImpl;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionManagerEx;
import com.intellij.openapi.actionSystem.impl.PresentationFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.keymap.KeyMapBundle;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.keymap.impl.ui.ShortcutTextField;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.StatusBarEx;
import com.intellij.openapi.wm.impl.FloatingDecorator;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.ui.popup.AbstractPopup;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.im.InputContext;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * This class is automaton with finite number of state.
 *
 * @author Anton Katilin
 * @author Vladimir Kondratyev
 */
public final class IdeKeyEventDispatcher implements Disposable {
  @NonNls
  private static final String GET_CACHED_STROKE_METHOD_NAME = "getCachedStroke";

  private static final int STATE_INIT = 0;
  private static final int STATE_WAIT_FOR_SECOND_KEYSTROKE = 1;
  private static final int STATE_SECOND_STROKE_IN_PROGRESS = 2;
  private static final int STATE_PROCESSED = 3;
  private static final int STATE_KEY_GESTURE_PROCESSOR = 4;

  private KeyStroke myFirstKeyStroke;
  /**
   * When we "dispatch" key event via keymap, i.e. when registered action has been executed
   * instead of event dispatching, then we have to consume all following KEY_RELEASED and
   * KEY_TYPED event because they are not valid.
   */
  private boolean myPressedWasProcessed;
  private int myState = STATE_INIT;

  private final ArrayList<AnAction> myActions = new ArrayList<AnAction>();
  private final PresentationFactory myPresentationFactory = new PresentationFactory();
  private JComponent myFoundComponent;
  private boolean myDisposed = false;
  private boolean myLeftCtrlPressed = false;
  private boolean myRightAltPressed = false;

  private KeyboardGestureProcessor myKeyGestureProcessor = new KeyboardGestureProcessor(this);

  public IdeKeyEventDispatcher(){
    Disposer.register(ApplicationManager.getApplication(), this);
  }

  public boolean isWaitingForSecondKeyStroke(){
    return myState == STATE_WAIT_FOR_SECOND_KEYSTROKE || myPressedWasProcessed;
  }

  /**
   * @return <code>true</code> if and only if the passed event is already dispatched by the
   * <code>IdeKeyEventDispatcher</code> and there is no need for any other processing of the event.
   */
  public boolean dispatchKeyEvent(final KeyEvent e){
    if (myDisposed) return false;

    if(e.isConsumed()){
      return false;
    }

    // http://www.jetbrains.net/jira/browse/IDEADEV-12372
    if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
      if (e.getID() == KeyEvent.KEY_PRESSED) {
        myLeftCtrlPressed = e.getKeyLocation() == KeyEvent.KEY_LOCATION_LEFT;
      }
      else if (e.getID() == KeyEvent.KEY_RELEASED) {
        myLeftCtrlPressed = false;
      }
    }
    else if (e.getKeyCode() == KeyEvent.VK_ALT) {
      if (e.getID() == KeyEvent.KEY_PRESSED) {
        myRightAltPressed = e.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT;
      }
      else if (e.getID() == KeyEvent.KEY_RELEASED) {
        myRightAltPressed = false;
      }
    }

    KeyboardFocusManager focusManager=KeyboardFocusManager.getCurrentKeyboardFocusManager();
    Component focusOwner = focusManager.getFocusOwner();

    // shortcuts should not work in shortcut setup fields
    if (focusOwner instanceof ShortcutTextField) {
      return false;
    }

    MenuSelectionManager menuSelectionManager=MenuSelectionManager.defaultManager();
    MenuElement[] selectedPath = menuSelectionManager.getSelectedPath();
    if(selectedPath.length>0){
      if (!(selectedPath[0] instanceof ComboPopup)) {
        // The following couple of lines of code is a PATCH!!!
        // It is needed to ignore ENTER KEY_TYPED events which sometimes can reach editor when an action
        // is invoked from main menu via Enter key.
        myState=STATE_PROCESSED;
        myPressedWasProcessed=true;
        return false;
      }
    }

    // Keymap shortcuts (i.e. not local shortcuts) should work only in:
    // - main frame
    // - floating focusedWindow
    // - when there's an editor in contexts
    Window focusedWindow = focusManager.getFocusedWindow();
    boolean isModalContext = focusedWindow != null && isModalContext(focusedWindow);

    final DataManager dataManager = DataManager.getInstance();
    if (dataManager == null) return false;

    DataContext dataContext = dataManager.getDataContext();

    if (myState == STATE_INIT) {
      return inInitState(focusOwner, e, isModalContext, dataContext);
    } else if (myState == STATE_PROCESSED) {
      return inProcessedState(focusOwner, e, isModalContext, dataContext);
    } else if (myState == STATE_WAIT_FOR_SECOND_KEYSTROKE) {
      return inWaitForSecondStrokeState(e, isModalContext, dataContext);
    } else if (myState == STATE_SECOND_STROKE_IN_PROGRESS) {
      return inSecondStrokeInProgressState(e, isModalContext, dataContext);
    } else if (myState == STATE_KEY_GESTURE_PROCESSOR) {
      return myKeyGestureProcessor.process(e, isModalContext, dataContext);
    } else {
      throw new IllegalStateException("state = " + myState);
    }
  }

  /**
   * @return <code>true</code> if and only if the <code>component</code> represents
   * modal context.
   * @throws IllegalArgumentException if <code>component</code> is <code>null</code>.
   */
  public static boolean isModalContext(@NotNull Component component) {
    Window window;
    if (component instanceof Window) {
      window = (Window)component;
    } else {
      window = SwingUtilities.getWindowAncestor(component);
    }

    if (window instanceof JDialog) {
      final JDialog dialog = (JDialog)window;
      if (!dialog.isModal()) {
        return isModalContext(dialog.getOwner());
      }
    }

    boolean isMainFrame = window instanceof IdeFrameImpl;
    boolean isFloatingDecorator = window instanceof FloatingDecorator;

    boolean isPopup = !(component instanceof JFrame) && !(component instanceof JDialog);
    if (isPopup) {
      if (component instanceof JWindow) {
        JBPopup popup = (JBPopup)((JWindow)component).getRootPane().getClientProperty(AbstractPopup.KEY);
        if (popup != null) {
          return popup.isModalContext();
        }
      }
    }

    return !isMainFrame && !isFloatingDecorator;
  }

  private boolean inWaitForSecondStrokeState(KeyEvent e, boolean isModalContext, DataContext dataContext) {
    // a key pressed means that the user starts to enter the second stroke...
    if (KeyEvent.KEY_PRESSED==e.getID()) {
      myState=STATE_SECOND_STROKE_IN_PROGRESS;
      return inSecondStrokeInProgressState(e, isModalContext, dataContext);
    }
    // looks like RELEASEs (from the first stroke) go here...  skip them
    return true;
  }

  /**
   * This is hack. AWT doesn't allow to create KeyStroke with specified key code and key char
   * simultaneously. Therefore we are using reflection.
   */
  private static KeyStroke getKeyStrokeWithoutMouseModifiers(KeyStroke originalKeyStroke){
    int modifier=originalKeyStroke.getModifiers()&~InputEvent.BUTTON1_DOWN_MASK&~InputEvent.BUTTON1_MASK&
                 ~InputEvent.BUTTON2_DOWN_MASK&~InputEvent.BUTTON2_MASK&
                 ~InputEvent.BUTTON3_DOWN_MASK&~InputEvent.BUTTON3_MASK;
    try {
      Method[] methods=AWTKeyStroke.class.getDeclaredMethods();
      Method getCachedStrokeMethod=null;
      for (Method method : methods) {
        if (GET_CACHED_STROKE_METHOD_NAME.equals(method.getName())) {
          getCachedStrokeMethod = method;
          getCachedStrokeMethod.setAccessible(true);
          break;
        }
      }
      if(getCachedStrokeMethod==null){
        throw new IllegalStateException("not found method with name getCachedStrokeMethod");
      }
      Object[] getCachedStrokeMethodArgs=new Object[]{originalKeyStroke.getKeyChar(), originalKeyStroke.getKeyCode(), modifier, originalKeyStroke.isOnKeyRelease()};
      KeyStroke keyStroke=(KeyStroke)getCachedStrokeMethod.invoke(
        originalKeyStroke,
        getCachedStrokeMethodArgs
      );
      return keyStroke;
    }catch(Exception exc){
      throw new IllegalStateException(exc.getMessage());
    }
  }

  private boolean inSecondStrokeInProgressState(KeyEvent e, boolean isModalContext, DataContext dataContext) {
    // when any key is released, we stop waiting for the second stroke
    if(KeyEvent.KEY_RELEASED==e.getID()){
      myFirstKeyStroke=null;
      myState=STATE_INIT;
      Project project = PlatformDataKeys.PROJECT.getData(dataContext);
      WindowManager.getInstance().getStatusBar(project).setInfo(null);
      return false;
    }

    KeyStroke originalKeyStroke=KeyStroke.getKeyStrokeForEvent(e);
    KeyStroke keyStroke=getKeyStrokeWithoutMouseModifiers(originalKeyStroke);

    fillActionsList(myFoundComponent, new KeyboardShortcut(myFirstKeyStroke, keyStroke), isModalContext);

    // consume the wrong second stroke and keep on waiting
    if (myActions.isEmpty()) {
      return true;
    }

    // finally user had managed to enter the second keystroke, so let it be processed
    Project project = PlatformDataKeys.PROJECT.getData(dataContext);
    StatusBarEx statusBar = (StatusBarEx) WindowManager.getInstance().getStatusBar(project);
    if (processAction(e, dataContext)) {
      statusBar.setInfo(null);
      return true;
    } else {
      return false;
    }
  }

  private boolean inProcessedState(Component focusOwner, final KeyEvent e, boolean isModalContext, DataContext dataContext) {
    // ignore typed events which come after processed pressed event
    if (KeyEvent.KEY_TYPED == e.getID() && myPressedWasProcessed) {
      return true;
    }
    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.VK_ALT == e.getKeyCode() && myPressedWasProcessed) {
      //see IDEADEV-8615
      return true;
    }
    myState = STATE_INIT;
    myPressedWasProcessed = false;
    return inInitState(focusOwner, e, isModalContext, dataContext);
  }

  private boolean inInitState(final Component focusOwner, KeyEvent e, boolean isModalContext, DataContext dataContext) {
    // http://www.jetbrains.net/jira/browse/IDEADEV-12372
    if (myLeftCtrlPressed && myRightAltPressed && focusOwner != null && e.getModifiers() == (InputEvent.CTRL_MASK | InputEvent.ALT_MASK)) {
      final InputContext inputContext = focusOwner.getInputContext();
      if (inputContext != null) {
        @NonNls final String language = inputContext.getLocale().getLanguage();
        if (language.equals("pl") ||
            language.equals("de") ||
            language.equals("fi") ||
            language.equals("fr") ||
            language.equals("no") ||
            language.equals("da") ||
            language.equals("se") ||
            language.equals("pt") ||
            language.equals("nl") ||
            language.equals("tr") ||
            language.equals("sl")) {
          // don't search for shortcuts
          return false;
        }
      }
    }

    KeyStroke originalKeyStroke=KeyStroke.getKeyStrokeForEvent(e);
    KeyStroke keyStroke=getKeyStrokeWithoutMouseModifiers(originalKeyStroke);

    //if (myKeyGestureProcessor.processInitState(focusOwner, e, isModalContext, dataContext)) return true;

    boolean hasSecondStroke = fillActionsList(focusOwner, new KeyboardShortcut(keyStroke, null), isModalContext);

    if(myActions.isEmpty()) {
      if (SystemInfo.isMac) {
        if (e.getID() == KeyEvent.KEY_PRESSED && e.getModifiersEx() == InputEvent.ALT_DOWN_MASK && hasMnemonicInWindow(focusOwner, e.getKeyCode())) {
          myPressedWasProcessed = true;
          myState = STATE_PROCESSED;
          return false;
        }
      }

      // there's nothing mapped for this stroke
      return false;
    }

    if(hasSecondStroke){
      myFirstKeyStroke=keyStroke;
      ArrayList<Pair<AnAction, KeyStroke>> secondKeyStorkes = new ArrayList<Pair<AnAction,KeyStroke>>();
      for (AnAction action : myActions) {
        Shortcut[] shortcuts = action.getShortcutSet().getShortcuts();
        for (Shortcut shortcut : shortcuts) {
          if (shortcut instanceof KeyboardShortcut) {
            KeyboardShortcut keyShortcut = (KeyboardShortcut)shortcut;
            if (keyShortcut.getFirstKeyStroke().equals(myFirstKeyStroke)) {
              secondKeyStorkes.add(new Pair<AnAction, KeyStroke>(action, keyShortcut.getSecondKeyStroke()));
            }
          }
        }
      }

      Project project = PlatformDataKeys.PROJECT.getData(dataContext);
      StringBuilder message = new StringBuilder();
      message.append(KeyMapBundle.message("prefix.key.pressed.message"));
      message.append(' ');
      for (int i = 0; i < secondKeyStorkes.size(); i++) {
        Pair<AnAction, KeyStroke> pair = secondKeyStorkes.get(i);
        if (i > 0) message.append(", ");
        message.append(pair.getFirst().getTemplatePresentation().getText());
        message.append(" (");
        message.append(KeymapUtil.getKeystrokeText(pair.getSecond()));
        message.append(")");
      }

      WindowManager.getInstance().getStatusBar(project).setInfo(message.toString());
      myState=STATE_WAIT_FOR_SECOND_KEYSTROKE;
      return true;
    }else{
      return processAction(e, dataContext);
    }
  }

  private static boolean hasMnemonicInWindow(Component focusOwner, int keyCode) {
    if (keyCode == KeyEvent.VK_ALT || keyCode == 0) return false; // Optimization
    final Container container = getContainer(focusOwner);
    return hasMnemonic(container, keyCode);
  }

  @Nullable
  private static Container getContainer(@Nullable final Component focusOwner) {
    if (focusOwner == null) return null;
    if (focusOwner.isLightweight()) {
      Container container = focusOwner.getParent();
      while (container != null) {
        final Container parent = container.getParent();
        if (parent instanceof JLayeredPane) break;
        if (parent != null && parent.isLightweight()) {
          container = parent;
        }
        else {
          break;
        }
      }
      return container;
    }

    return SwingUtilities.windowForComponent(focusOwner);
  }

  private static boolean hasMnemonic(final Container container, final int keyCode) {
    if (container == null) return false;

    final Component[] components = container.getComponents();
    for (Component component : components) {
      if (component instanceof AbstractButton) {
        final AbstractButton button = (AbstractButton)component;
        if (button.getMnemonic() == keyCode) return true;
      }
      if (component instanceof Container) {
        if (hasMnemonic((Container)component, keyCode)) return true;
      }
    }
    return false;
  }

  private boolean processAction(final KeyEvent e, DataContext dataContext) {
    ActionManagerEx actionManager = ActionManagerEx.getInstanceEx();
    for (final AnAction action : myActions) {
      final Presentation presentation = myPresentationFactory.getPresentation(action);

      // Mouse modifiers are 0 because they have no any sense when action is invoked via keyboard
      final AnActionEvent actionEvent =
        new AnActionEvent(e, dataContext, ActionPlaces.MAIN_MENU, presentation, ActionManager.getInstance(), 0);
      action.beforeActionPerformedUpdate(actionEvent);
      if (!presentation.isEnabled()) {
        continue;
      }

      myState = STATE_PROCESSED;
      myPressedWasProcessed = e.getID() == KeyEvent.KEY_PRESSED;

      ((DataManagerImpl.MyDataContext)dataContext).setEventCount(IdeEventQueue.getInstance().getEventCount());
      actionManager.fireBeforeActionPerformed(action, actionEvent.getDataContext());
      Component component = (Component)actionEvent.getDataContext().getData(DataConstants.CONTEXT_COMPONENT);
      if (component != null && !component.isShowing()) {
        return true;
      }
      e.consume();
      action.actionPerformed(actionEvent);
      return true;
    }

    return false;
  }

  /**
   * This method fills <code>myActions</code> list.
   * @return true if there is a shortcut with second stroke found.
   */
  public boolean fillActionsList(Component component, Shortcut sc, boolean isModalContext){
    myFoundComponent = null;
    myActions.clear();

    boolean hasSecondStroke = false;

    // here we try to find "local" shortcuts

    for (; component != null; component = component.getParent()) {
      if (!(component instanceof JComponent)) {
        continue;
      }
      ArrayList listOfActions = (ArrayList)((JComponent)component).getClientProperty(AnAction.ourClientProperty);
      if (listOfActions == null) {
        continue;
      }
      for (Object listOfAction : listOfActions) {
        if (!(listOfAction instanceof AnAction)) {
          continue;
        }
        AnAction action = (AnAction)listOfAction;
        hasSecondStroke |= addAction(action, sc);
      }
      // once we've found a proper local shortcut(s), we continue with non-local shortcuts
      if (!myActions.isEmpty()) {
        myFoundComponent = (JComponent)component;
        break;
      }
    }

    // search in main keymap

    String[] actionIds;
    Keymap keymap = KeymapManager.getInstance().getActiveKeymap();
    actionIds = keymap.getActionIds(sc);

    ActionManager actionManager = ActionManager.getInstance();
    for (String actionId : actionIds) {
      AnAction action = actionManager.getAction(actionId);
      if (action != null) {
        if (isModalContext && !action.isEnabledInModalContext()) {
          continue;
        }
        hasSecondStroke |= addAction(action, sc);
      }
    }
    return hasSecondStroke;
  }

  /**
   * @return true if action is added and has second stroke
   */
  private boolean addAction(AnAction action, Shortcut sc) {
    boolean hasSecondStroke = false;

    Shortcut[] shortcuts = action.getShortcutSet().getShortcuts();
    for (Shortcut each : shortcuts) {
      if (!each.isKeyboard()) continue;

      if (each.startsWith(sc)) {
        if (!myActions.contains(action)) {
          myActions.add(action);
        }

        if (each instanceof KeyboardShortcut) {
          hasSecondStroke |= ((KeyboardShortcut)each).getSecondKeyStroke() != null;
        }
      }
    }

    return hasSecondStroke;
  }

  public void dispose() {
    myDisposed = true;
  }
}