package com.intellij.cvsSupport2.cvsBrowser.ui;

import com.intellij.CvsBundle;
import com.intellij.pom.Navigatable;
import com.intellij.idea.ActionsBundle;
import com.intellij.cvsSupport2.CvsVcs2;
import com.intellij.cvsSupport2.actions.cvsContext.CvsContextAdapter;
import com.intellij.cvsSupport2.actions.cvsContext.CvsDataConstants;
import com.intellij.cvsSupport2.actions.cvsContext.CvsLightweightFile;
import com.intellij.cvsSupport2.changeBrowser.CvsCommittedChangesProvider;
import com.intellij.cvsSupport2.checkout.CheckoutAction;
import com.intellij.cvsSupport2.config.CvsConfiguration;
import com.intellij.cvsSupport2.config.CvsRootConfiguration;
import com.intellij.cvsSupport2.cvsBrowser.CheckoutHelper;
import com.intellij.cvsSupport2.cvsBrowser.CvsElement;
import com.intellij.cvsSupport2.cvsBrowser.CvsTree;
import com.intellij.cvsSupport2.cvshandlers.CommandCvsHandler;
import com.intellij.cvsSupport2.cvshandlers.CvsHandler;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.vfs.VcsVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.peer.PeerFactory;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.UIHelper;
import com.intellij.util.ui.tree.TreeUtil;
import com.intellij.util.OpenSourceUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.io.File;

/**
 * author: lesya
 */
public class BrowserPanel extends JPanel implements DataProvider {
  private final CvsTree myTree;
  private final CheckoutHelper myCheckoutHelper;
  private final CvsRootConfiguration myCvsRootConfiguration;
  private final Project myProject;

  public BrowserPanel(CvsRootConfiguration configuration, Project project) {
    super(new BorderLayout(2, 0));
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    myProject = project;
    myCvsRootConfiguration = configuration;
    myTree = new CvsTree(configuration, project, true, TreeSelectionModel.SINGLE_TREE_SELECTION, false, true);
    add(myTree, BorderLayout.CENTER);
    myTree.init();
    myCheckoutHelper = new CheckoutHelper(configuration, this);

    UIHelper uiHelper = PeerFactory.getInstance().getUIHelper();

    uiHelper.installToolTipHandler(myTree.getTree());
    uiHelper.installEditSourceOnDoubleClick(myTree.getTree());
    TreeUtil.installActions(myTree.getTree());

    ActionGroup group = getActionGroup();
    PopupHandler.installPopupHandler(myTree.getTree(), group, ActionPlaces.CHECKOUT_POPUP, ActionManager.getInstance());
  }

  public ActionGroup getActionGroup() {
    DefaultActionGroup result = new DefaultActionGroup();
    result.add(new EditSourceAction());
    result.add(new MyCheckoutAction());
    //TODO lesya
    //result.add(new ShowLightCvsFileHistoryAction());
    result.add(new MyAnnotateAction());
    result.add(new BrowseChangesAction());
    return result;
  }

  private class EditSourceAction extends AnAction {
    public EditSourceAction() {
      super(ActionsBundle.actionText("EditSource"),
            ActionsBundle.actionDescription("EditSource"),
            IconLoader.getIcon("/actions/editSource.png"));
    }

    public void actionPerformed(AnActionEvent e) {
      final Navigatable[] navigatableArray = e.getData(DataKeys.NAVIGATABLE_ARRAY);
      if (navigatableArray != null && navigatableArray.length > 0) {
        OpenSourceUtil.navigate(navigatableArray, true);
      }
    }

    public void update(final AnActionEvent e) {
      final Navigatable[] navigatableArray = e.getData(DataKeys.NAVIGATABLE_ARRAY);
      e.getPresentation().setEnabled(navigatableArray != null && navigatableArray.length > 0);
    }
  }

  private class MyCheckoutAction extends AnAction {
    public MyCheckoutAction() {
      super(CvsBundle.message("operation.name.check.out"), null, IconLoader.getIcon("/actions/checkOut.png"));
    }

    public void update(AnActionEvent e) {
      Presentation presentation = e.getPresentation();
      presentation.setEnabled(canPerformCheckout());
    }

    private boolean canPerformCheckout() {
      CvsElement[] currentSelection = myTree.getCurrentSelection();
      return (currentSelection.length == 1) && currentSelection[0].canBeCheckedOut();
    }

    public void actionPerformed(AnActionEvent e) {
      CvsElement[] cvsElements = myTree.getCurrentSelection();

      CvsElement selectedElement = cvsElements[0];
      if (!myCheckoutHelper.prepareCheckoutData(selectedElement, false, null)) {
        return;
      }
      CvsHandler checkoutHandler = CommandCvsHandler.createCheckoutHandler(
        myCvsRootConfiguration,
        new String[]{selectedElement.getCheckoutPath()},
        myCheckoutHelper.getCheckoutLocation(),
        false, CvsConfiguration.getInstance(myProject).MAKE_NEW_FILES_READONLY
      );

      CvsContextAdapter context = new CvsContextAdapter() {
        public Project getProject() {
          return myProject;
        }
      };

      new CheckoutAction().actionPerformed(context, checkoutHandler);
    }
  }

  private class MyAnnotateAction extends AnAction {
    public MyAnnotateAction() {
      super(CvsBundle.message("operation.name.annotate"), null, IconLoader.getIcon("/actions/annotate.png"));
    }

    public void update(AnActionEvent e) {
      Presentation presentation = e.getPresentation();
      presentation.setVisible(true);
      CvsLightweightFile cvsLightFile = getCvsLightFile();
      if (cvsLightFile != null) {
        File file = cvsLightFile.getCvsFile();
        presentation.setEnabled(file != null);
      } else {
        presentation.setEnabled(false);
      }
    }

    public void actionPerformed(AnActionEvent e) {
      VcsVirtualFile vcsVirtualFile = (VcsVirtualFile)getCvsVirtualFile();
      try {
        final FileAnnotation annotation = CvsVcs2.getInstance(myProject)
            .createAnnotation(vcsVirtualFile, vcsVirtualFile.getRevision(), myCvsRootConfiguration);
        AbstractVcsHelper.getInstance(myProject).showAnnotation(annotation, vcsVirtualFile);
      }
      catch (VcsException e1) {
        AbstractVcsHelper.getInstance(myProject).showError(e1, CvsBundle.message("operation.name.annotate"));
      }
    }
  }

  private class BrowseChangesAction extends AnAction {
    public BrowseChangesAction() {
      super(VcsBundle.message("browse.changes.action"), "", IconLoader.getIcon("/actions/showChangesOnly.png"));
    }

    public void actionPerformed(AnActionEvent e) {
      CvsElement[] currentSelection = myTree.getCurrentSelection();
      assert currentSelection.length == 1;
      final String moduleName = currentSelection [0].getElementPath();
      CvsCommittedChangesProvider provider = new CvsCommittedChangesProvider(myProject, myCvsRootConfiguration, moduleName);
      AbstractVcsHelper.getInstance(myProject).showChangesBrowser(provider, null, VcsBundle.message("browse.changes.scope", moduleName), BrowserPanel.this);
    }

    public void update(final AnActionEvent e) {
      CvsElement[] currentSelection = myTree.getCurrentSelection();
      e.getPresentation().setEnabled(currentSelection.length == 1);
    }
  }

  public Object getData(String dataId) {
    if (DataConstants.NAVIGATABLE.equals(dataId)) {
      VirtualFile cvsVirtualFile = getCvsVirtualFile();
      if (cvsVirtualFile == null || !cvsVirtualFile.isValid()) return null;
      return new OpenFileDescriptor(myProject, cvsVirtualFile);
    }
    else if (DataConstants.PROJECT.equals(dataId)) {
      return myProject;
    }
    else if (CvsDataConstants.CVS_ENVIRONMENT.equals(dataId)) {
      return myCvsRootConfiguration;
    }
    else if (CvsDataConstants.CVS_LIGHT_FILE.equals(dataId)) {
      return getCvsLightFile();
    }
    else {
      return null;
    }
  }

  @Nullable
  private VirtualFile getCvsVirtualFile() {
    CvsElement[] currentSelection = myTree.getCurrentSelection();
    if (currentSelection.length != 1) return null;
    VirtualFile file = currentSelection[0].getVirtualFile();
    if (file == null) return null;
    if (file.isDirectory()) return null;
    return file;
  }

  @Nullable
  private CvsLightweightFile getCvsLightFile() {
    CvsElement[] currentSelection = myTree.getCurrentSelection();
    if (currentSelection.length != 1) return null;
    return new CvsLightweightFile(currentSelection[0].getCvsLightFile(), null);
  }
}