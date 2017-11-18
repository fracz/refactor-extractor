/*
 * Copyright (c) 2000-2004 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.diagnostic;

/**
 * @author kir, max
 */

import com.intellij.ExtensionPoints;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.reporter.ScrData;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.text.DateFormatUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

public class IdeErrorsDialog extends DialogWrapper implements MessagePoolListener {
  private JTextPane myDetailsPane;
  private List myFatalErrors;
  private List<ArrayList<AbstractMessage>> myModel = new ArrayList<ArrayList<AbstractMessage>>();
  private final MessagePool myMessagePool;
  private JLabel myCountLabel;
  private JLabel myBlameLabel;
  private JLabel myInfoLabel;
  private JCheckBox myImmediatePopupCheckbox;

  private int myIndex = 0;
  public static final String IMMEDIATE_POPUP_OPTION = "IMMEDIATE_FATAL_ERROR_POPUP";

  public IdeErrorsDialog(MessagePool messagePool) {
    super(JOptionPane.getRootFrame(), false);

    myMessagePool = messagePool;

    init();
  }

  public void newEntryAdded() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        rebuildHeaders();
      }
    });
  }

  public void poolCleared() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        doOKAction();
      }
    });
  }

  protected Action[] createActions() {
    return new Action[]{new ShutdownAction(), new ClearFatalsAction(), new CloseAction()};
  }

  private ActionToolbar createNavigationToolbar() {
    DefaultActionGroup group = new DefaultActionGroup();

    BackAction back = new BackAction();
    back.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0)), getRootPane());
    group.add(back);

    ForwardAction forward = new ForwardAction();
    forward.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0)), getRootPane());
    group.add(forward);


    return ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, true);
  }

  private void goBack() {
    myIndex--;
    updateControls();
  }

  private void goForward() {
    myIndex++;
    updateControls();
  }

  private void updateControls() {
    updateCountLabel();
    updateBlameLabel();
    updateInfoLabel();
    updateDetailsPane();
  }

  private void updateInfoLabel() {
    final AbstractMessage message = getMessageAt(myIndex);
    if (message != null) {
      StringBuffer txt = new StringBuffer();
      txt.append(DateFormatUtil.formatDate(new Date(), message.getDate()));
      txt.append(". Occured ");
      int occ = myModel.get(myIndex).size();
      txt.append(occ == 1 ? "once" : Integer.toString(occ) + " times");
      txt.append(" since last reset. ");
      if (message.isSumbitted()) {
        final SubmittedReportInfo info = message.getSubmissionInfo();
        if (info.getStatus() == SubmittedReportInfo.SubmissionStatus.FAILED) {
          txt.append("Submission failed");
        }
        else {
          txt.append("Submitted");
          if (info.getLinkText() != null) {
            txt.append(" as " + info.getLinkText());
            if (info.getStatus() == SubmittedReportInfo.SubmissionStatus.DUPLICATE) {
              txt.append(" [Duplicate]");
            }
          }
        }
        txt.append(". ");
      }
      else if (!message.isRead()) {
        txt.append("Unread. ");
      }
      myInfoLabel.setText(txt.toString());
    }
    else {
      myInfoLabel.setText("");
    }
  }

  private void updateBlameLabel() {
    final AbstractMessage message = getMessageAt(myIndex);
    if (message != null) {
      final String pluginName = findPluginName(message.getThrowable());
      myBlameLabel.setText("Blame " + (pluginName == null ? "IDEA core" : pluginName));
    }
    else {
      myBlameLabel.setText("");
    }
  }

  private void updateDetailsPane() {
    final AbstractMessage message = getMessageAt(myIndex);
    if (message != null) {
      showMessageDetails(message);
    }
    else {
      hideMessageDetails();
    }
  }

  private void updateCountLabel() {
    myCountLabel.setText(Integer.toString(myIndex + 1) + " of " + myModel.size());
  }

  private class BackAction extends AnAction {
    public BackAction() {
      super("Back", null, IconLoader.getIcon("/actions/back.png"));
    }

    public void actionPerformed(AnActionEvent e) {
      goBack();
    }


    public void update(AnActionEvent e) {
      Presentation presentation = e.getPresentation();
      presentation.setEnabled(myIndex > 0);
    }
  }

  private class ForwardAction extends AnAction {
    public ForwardAction() {
      super("Forward", null, IconLoader.getIcon("/actions/forward.png"));
    }

    public void actionPerformed(AnActionEvent e) {
      goForward();
    }

    public void update(AnActionEvent e) {
      Presentation presentation = e.getPresentation();
      presentation.setEnabled(myIndex < myModel.size() - 1);
    }
  }

  protected JComponent createCenterPanel() {
    setTitle("IDE Fatal Errors");

    JPanel root = new JPanel(new BorderLayout());
    JPanel top = new JPanel(new BorderLayout());
    JPanel toolbar = new JPanel(new FlowLayout());

    myImmediatePopupCheckbox = new JCheckBox("Popup this window immediately next time internal error occurs");
    myImmediatePopupCheckbox.setSelected("true".equals(PropertiesComponent.getInstance().getValue(IMMEDIATE_POPUP_OPTION)));

    myCountLabel = new JLabel();
    myBlameLabel = new JLabel();
    myInfoLabel = new JLabel();
    ActionToolbar navToolbar = createNavigationToolbar();
    toolbar.add(navToolbar.getComponent());
    toolbar.add(myCountLabel);
    top.add(toolbar, BorderLayout.WEST);

    JPanel blamePanel = new JPanel(new FlowLayout());
    blamePanel.add(myBlameLabel);
    final ActionToolbar blameToolbar = createBlameToolbar();
    blamePanel.add(blameToolbar.getComponent());
    top.add(blamePanel, BorderLayout.EAST);

    root.add(top, BorderLayout.NORTH);

    myDetailsPane = new JTextPane();
    myDetailsPane.setEditable(false);
    JPanel infoPanel = new JPanel(new BorderLayout());
    JPanel gapPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
    gapPanel.add(myInfoLabel);
    infoPanel.add(gapPanel, BorderLayout.NORTH);
    infoPanel.add(new JScrollPane(myDetailsPane), BorderLayout.CENTER);
    root.add(infoPanel, BorderLayout.CENTER);
    root.add(myImmediatePopupCheckbox, BorderLayout.SOUTH);

    root.setPreferredSize(new Dimension(600, 550));

    rebuildHeaders();
    moveSelectionToEarliestMessage();
    updateControls();
    return root;
  }

  private ActionToolbar createBlameToolbar() {
    DefaultActionGroup blameGroup = new DefaultActionGroup();
    final BlameAction blameAction = new BlameAction();
    blameGroup.add(blameAction);
    blameAction.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)), getRootPane());

    final ViewRequestAction viewRequestAction = new ViewRequestAction();
    blameGroup.add(viewRequestAction);
    // These two cannot be enabled simultaneously this we can assign same shortcut.
    viewRequestAction.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)), getRootPane());

    final ActionToolbar blameToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, blameGroup, true);
    return blameToolbar;
  }

  private AbstractMessage getMessageAt(int idx) {
    if (idx < 0 || idx >= myModel.size()) return null;
    return myModel.get(idx).get(0);
  }

  private void moveSelectionToEarliestMessage() {
    myIndex = 0;
    for (int i = 0; i < myModel.size(); i++) {
      final AbstractMessage each = getMessageAt(i);
      if (!each.isRead()) {
        myIndex = i;
        break;
      }
    }

    updateControls();
  }

  private void rebuildHeaders() {
    myModel.clear();
    myFatalErrors = myMessagePool.getFatalErrors(true, true);

    Map<String, ArrayList<AbstractMessage>> hash2Messages = buildHashcode2MessageListMap(myFatalErrors);

    final Iterator<ArrayList<AbstractMessage>> messageLists = hash2Messages.values().iterator();
    while (messageLists.hasNext()) {
      myModel.add(messageLists.next());
    }

    updateControls();
  }

  private Map<String, ArrayList<AbstractMessage>> buildHashcode2MessageListMap(List aErrors) {
    Map<String, ArrayList<AbstractMessage>> hash2Messages = new LinkedHashMap<String, ArrayList<AbstractMessage>>();
    for (int i = 0; i < aErrors.size(); i++) {
      final AbstractMessage each = (AbstractMessage)aErrors.get(i);
      final String hashcode = ScrData.getThrowableHashCode(each.getThrowable());
      ArrayList<AbstractMessage> list;
      if (hash2Messages.containsKey(hashcode)) {
        list = hash2Messages.get(hashcode);
      }
      else {
        list = new ArrayList<AbstractMessage>();
        hash2Messages.put(hashcode, list);
      }
      list.add(0, each);
    }
    return hash2Messages;
  }

  private void showMessageDetails(AbstractMessage aMessage) {
    myDetailsPane.setText(new StringBuffer().append(aMessage.getMessage()).append("\n").append(aMessage.getThrowableText()).toString());
    if (myDetailsPane.getCaret() != null) { // Upon some strange circumstances caret may be missing from the text component making the following line fail with NPE.
      myDetailsPane.setCaretPosition(0);
    }
  }

  private void hideMessageDetails() {
    myDetailsPane.setText("");
  }

  public static String findPluginName(Throwable t) {
    StackTraceElement[] elements = t.getStackTrace();
    for (int i = 0; i < elements.length; i++) {
      StackTraceElement element = elements[i];
      String className = element.getClassName();
      if (PluginManager.isPluginClass(className)) {
        return PluginManager.getPluginByClassName(className);
      }
    }

    if (t instanceof NoSuchMethodException) {
      // check is method called from plugin classes
      if (t.getMessage() != null) {
        String className = "";
        StringTokenizer tok = new StringTokenizer(t.getMessage(), ".");
        while (tok.hasMoreTokens()) {
          String token = tok.nextToken();
          if (token.length() > 0 && Character.isJavaIdentifierStart(token.charAt(0))) {
            className += token;
          }
        }

        if (PluginManager.isPluginClass(className)) {
          return PluginManager.getPluginByClassName(className);
        }
      }
    }
    else if (t instanceof ClassNotFoundException) {
      // check is class from plugin classes
      if (t.getMessage() != null) {
        String className = t.getMessage();

        if (PluginManager.isPluginClass(className)) {
          return PluginManager.getPluginByClassName(className);
        }
      }
    }
    else if (t instanceof PluginException) {
      return ((PluginException)t).getDescriptor().getName();
    }

    return null;
  }

  private class ShutdownAction extends AbstractAction {
    public ShutdownAction() {
      super("Shut_down");
    }

    public void actionPerformed(ActionEvent e) {
      myMessagePool.setJvmIsShuttingDown();
      ApplicationManager.getApplication().exit();
    }
  }

  private class ClearFatalsAction extends AbstractAction {
    public ClearFatalsAction() {
      super("_Clear And Close");
    }

    public void actionPerformed(ActionEvent e) {
      myMessagePool.clearFatals();
      doOKAction();
    }
  }

  private class BlameAction extends AnAction {
    public BlameAction() {
      super("Submit", "Report to JetBrains", IconLoader.getIcon("/actions/startDebugger.png"));
    }

    public void update(AnActionEvent e) {
      final Presentation presentation = e.getPresentation();
      final AbstractMessage logMessage = getMessageAt(myIndex);
      if (logMessage == null) {
        presentation.setEnabled(false);
        return;
      }

      final ErrorReportSubmitter submitter = getSubmitter(logMessage);
      if (logMessage.isSumbitted() || submitter == null) {
        presentation.setEnabled(false);
        return;
      }
      presentation.setEnabled(true);
      presentation.setDescription(submitter.getReportActionText());
    }

    public void actionPerformed(AnActionEvent e) {
      final AbstractMessage logMessage = getMessageAt(myIndex);
      reportMessage(logMessage);
      rebuildHeaders();
      updateControls();
    }

    private void reportMessage(final AbstractMessage logMessage) {
      ErrorReportSubmitter submitter = getSubmitter(logMessage);

      if (submitter != null) {
        logMessage.setSubmitted(submitter.submit(getEvents(logMessage), getContentPane()));
      }
    }

    private IdeaLoggingEvent[] getEvents(final AbstractMessage logMessage) {
      if (logMessage instanceof GroupedLogMessage) {
        final List<AbstractMessage> messages = ((GroupedLogMessage)logMessage).getMessages();
        IdeaLoggingEvent[] res = new IdeaLoggingEvent[messages.size()];
        for (int i = 0; i < res.length; i++) {
          res[i] = getEvent(messages.get(i));
        }
        return res;
      }
      return new IdeaLoggingEvent[]{getEvent(logMessage)};
    }

    private IdeaLoggingEvent getEvent(final AbstractMessage logMessage) {
      return new IdeaLoggingEvent(logMessage.getMessage(), logMessage.getThrowable());
    }
  }

  private class ViewRequestAction extends AnAction {
    public ViewRequestAction() {
      super("Open in Browser", "Open related request page in browser", IconLoader.getIcon("/debugger/watches.png"));
    }

    public void update(AnActionEvent e) {
      final Presentation presentation = e.getPresentation();
      final AbstractMessage logMessage = getMessageAt(myIndex);
      if (logMessage == null) {
        presentation.setEnabled(false);
        return;
      }

      if (!logMessage.isSumbitted()) {
        presentation.setEnabled(false);
        return;
      }

      final SubmittedReportInfo info = logMessage.getSubmissionInfo();
      presentation.setEnabled(info.getURL() != null);
    }

    public void actionPerformed(AnActionEvent e) {
      final AbstractMessage logMessage = getMessageAt(myIndex);
      final SubmittedReportInfo info = logMessage.getSubmissionInfo();
      final String url = info.getURL();
      if (url != null) {
        BrowserUtil.launchBrowser(url);
      }
    }
  }

  private ErrorReportSubmitter getSubmitter(final AbstractMessage logMessage) {
    final String pluginName = findPluginName(logMessage.getThrowable());
    final Object[] reporters = Extensions.getRootArea().getExtensionPoint(ExtensionPoints.ERROR_HANDLER)
        .getExtensions();
    ErrorReportSubmitter submitter = null;
    for (int i = 0; i < reporters.length; i++) {
      ErrorReportSubmitter reporter = (ErrorReportSubmitter)reporters[i];
      final PluginDescriptor descriptor = reporter.getPluginDescriptor();
      if (pluginName == null && descriptor == null || descriptor != null && Comparing.equal(pluginName, descriptor.getPluginName())) {
        submitter = reporter;
      }
    }
    return submitter;
  }

  protected void doOKAction() {
    PropertiesComponent.getInstance().setValue(IMMEDIATE_POPUP_OPTION, String.valueOf(myImmediatePopupCheckbox.isSelected()));
    markAllAsRead();
    super.doOKAction();
  }

  private void markAllAsRead() {
    for (int i = 0; i < myFatalErrors.size(); i++) {
      AbstractMessage each = (AbstractMessage)myFatalErrors.get(i);
      each.setRead(true);
    }
  }

  public void doCancelAction() {
    PropertiesComponent.getInstance().setValue(IMMEDIATE_POPUP_OPTION, String.valueOf(myImmediatePopupCheckbox.isSelected()));
    markAllAsRead();
    super.doCancelAction();
  }

  protected class CloseAction extends AbstractAction {
    public CloseAction() {
      putValue(Action.NAME, "C_lose");
    }

    public void actionPerformed(ActionEvent e) {
      doOKAction();
    }
  }

  protected String getDimensionServiceKey() {
    return "IdeErrosDialog";
  }

}