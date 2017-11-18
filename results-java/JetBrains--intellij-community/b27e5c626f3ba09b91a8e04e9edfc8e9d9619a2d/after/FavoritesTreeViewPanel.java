package com.intellij.ide.favoritesTreeView;

import com.intellij.ide.*;
import com.intellij.ide.dnd.aware.DnDAwareTree;
import com.intellij.ide.projectView.impl.ModuleGroup;
import com.intellij.ide.projectView.impl.nodes.LibraryGroupElement;
import com.intellij.ide.projectView.impl.nodes.NamedLibraryElement;
import com.intellij.ide.projectView.impl.nodes.PackageElement;
import com.intellij.ide.ui.customization.CustomizableActionsSchemas;
import com.intellij.ide.util.DeleteHandler;
import com.intellij.ide.util.EditorHelper;
import com.intellij.ide.util.PackageUtil;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.history.LocalHistoryAction;
import com.intellij.history.LocalHistory;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.DataConstantsEx;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.actions.ModuleDeleteProvider;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.ui.*;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FavoritesTreeViewPanel extends JPanel implements DataProvider {
  @NonNls public static final String ABSTRACT_TREE_NODE_TRANSFERABLE = "AbstractTransferable";

  private FavoritesTreeStructure myFavoritesTreeStructure;
  private FavoritesViewTreeBuilder myBuilder;
  private CopyPasteManagerEx.CopyPasteDelegator myCopyPasteDelegator;
  private MouseListener myTreePopupHandler;
  @NonNls public static final String CONTEXT_FAVORITES_ROOTS = "FavoritesRoot";
  @NonNls public static final String FAVORITES_LIST_NAME = "FavoritesListName";

  protected Project myProject;
  private final String myHelpId;
  protected DnDAwareTree myTree;

  private final MyDeletePSIElementProvider myDeletePSIElementProvider = new MyDeletePSIElementProvider();
  private final ModuleDeleteProvider myDeleteModuleProvider = new ModuleDeleteProvider();

  private String myListName;
  private IdeView myIdeView = new MyIdeView();

  public FavoritesTreeViewPanel(Project project, String helpId, String name) {
    super(new BorderLayout());
    myProject = project;
    myHelpId = helpId;
    myListName = name;

    myFavoritesTreeStructure = new FavoritesTreeStructure(project, myListName);
    DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    root.setUserObject(myFavoritesTreeStructure.getRootElement());
    final DefaultTreeModel treeModel = new DefaultTreeModel(root);
    myTree = new DnDAwareTree(treeModel) {
      public void setRowHeight(int i) {
        super.setRowHeight(0);
      }
    };
    myBuilder = new FavoritesViewTreeBuilder(myProject, myTree, treeModel, myFavoritesTreeStructure, myListName);

    TreeUtil.installActions(myTree);
    UIUtil.setLineStyleAngled(myTree);
    myTree.setRootVisible(false);
    myTree.setShowsRootHandles(true);
    myTree.setLargeModel(true);
    new TreeSpeedSearch(myTree);
    ToolTipManager.sharedInstance().registerComponent(myTree);
    TreeToolTipHandler.install(myTree);
    myTree.setCellRenderer(new NodeRenderer() {
      public void customizeCellRenderer(JTree tree,
                                        Object value,
                                        boolean selected,
                                        boolean expanded,
                                        boolean leaf,
                                        int row,
                                        boolean hasFocus) {
        super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
        if (value instanceof DefaultMutableTreeNode) {
          final DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
          //only favorites roots to explain
          final Object userObject = node.getUserObject();
          if (userObject instanceof FavoritesTreeNodeDescriptor) {
            final FavoritesTreeNodeDescriptor favoritesTreeNodeDescriptor = (FavoritesTreeNodeDescriptor)userObject;
            AbstractTreeNode treeNode = favoritesTreeNodeDescriptor.getElement();
            final ItemPresentation presentation = treeNode.getPresentation();
            String locationString = presentation != null ? presentation.getLocationString() : null;
            if (locationString != null && locationString.length() > 0) {
              append(" (" + locationString + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
            }
            else if (node.getParent() != null && node.getParent().getParent() == null) {
              final String location = favoritesTreeNodeDescriptor.getLocation();
              if (location != null && location.length() > 0) {
                append(" (" + location + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
              }
            }
          }
        }
      }
    });
    JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(myTree);
    myTreePopupHandler = PopupHandler.installPopupHandler(myTree, (ActionGroup)CustomizableActionsSchemas.getInstance()
      .getCorrectedAction(IdeActions.GROUP_FAVORITES_VIEW_POPUP), ActionPlaces.FAVORITES_VIEW_POPUP, ActionManager.getInstance());
    add(scrollPane, BorderLayout.CENTER);
    //add(createActionsToolbar(), BorderLayout.NORTH);

    EditSourceOnDoubleClickHandler.install(myTree);
    myTree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (!e.isPopupTrigger() && e.getClickCount() == 2) {
          OpenSourceUtil.openSourcesFrom(DataManager.getInstance().getDataContext(FavoritesTreeViewPanel.this), true);
        }
      }
    });

    myTree.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          OpenSourceUtil.openSourcesFrom(DataManager.getInstance().getDataContext(FavoritesTreeViewPanel.this), false);
        }
      }
    });
    myCopyPasteDelegator = new CopyPasteManagerEx.CopyPasteDelegator(myProject, this) {
      @NotNull
      protected PsiElement[] getSelectedElements() {
        return getSelectedPsiElements();
      }
    };
  }

  public void updateTreePopupHandler() {
    myTree.removeMouseListener(myTreePopupHandler);
    ActionGroup group = (ActionGroup)CustomizableActionsSchemas.getInstance().getCorrectedAction(IdeActions.GROUP_FAVORITES_VIEW_POPUP);
    myTreePopupHandler = PopupHandler.installPopupHandler(myTree, group, ActionPlaces.FAVORITES_VIEW_POPUP, ActionManager.getInstance());
  }

  public void selectElement(final Object selector, final VirtualFile file) {
    myBuilder.select(selector, file, true);
  }

  public void dispose() {
    Disposer.dispose(myBuilder);
    myBuilder = null;
  }

  public DnDAwareTree getTree() {
    return myTree;
  }

  public String getName() {
    return myListName;
  }

  public void setName(String name) {
    myListName = name;
  }

  @NotNull
  private PsiElement[] getSelectedPsiElements() {
    final Object[] elements = getSelectedNodeElements();
    if (elements == null) {
      return PsiElement.EMPTY_ARRAY;
    }
    ArrayList<PsiElement> result = new ArrayList<PsiElement>();
    for (Object element : elements) {
      if (element instanceof PsiElement) {
        result.add((PsiElement)element);
      }
      else if (element instanceof SmartPsiElementPointer) {
        PsiElement psiElement = ((SmartPsiElementPointer)element).getElement();
        if (psiElement != null) {
          result.add(psiElement);
        }
      }
    }
    return result.toArray(new PsiElement[result.size()]);
  }

  public FavoritesTreeStructure getFavoritesTreeStructure() {
    return myFavoritesTreeStructure;
  }

  public Object getData(String dataId) {
    if (DataConstants.PROJECT.equals(dataId)) {
      return myProject;
    }
    if (DataConstants.NAVIGATABLE.equals(dataId)) {
      final FavoritesTreeNodeDescriptor[] selectedNodeDescriptors = getSelectedNodeDescriptors();
      return selectedNodeDescriptors.length == 1 ? selectedNodeDescriptors[0].getElement() : null;
    }
    if (DataConstants.NAVIGATABLE_ARRAY.equals(dataId)) {
      final List<Navigatable> selectedElements = getSelectedElements(Navigatable.class);
      return selectedElements.toArray(new Navigatable[selectedElements.size()]);
    }

    if (DataConstantsEx.CUT_PROVIDER.equals(dataId)) {
      return myCopyPasteDelegator.getCutProvider();
    }
    if (DataConstantsEx.COPY_PROVIDER.equals(dataId)) {
      return myCopyPasteDelegator.getCopyProvider();
    }
    if (DataConstantsEx.PASTE_PROVIDER.equals(dataId)) {
      return myCopyPasteDelegator.getPasteProvider();
    }
    if (DataConstants.HELP_ID.equals(dataId)) {
      return myHelpId;
    }
    if (DataConstants.PSI_ELEMENT.equals(dataId)) {
      Object[] elements = getSelectedNodeElements();
      if (elements == null || elements.length != 1) {
        return null;
      }
      final PsiElement psiElement;
      if (elements[0] instanceof SmartPsiElementPointer) {
        psiElement = ((SmartPsiElementPointer)elements[0]).getElement();
      }
      else if (elements[0] instanceof PsiElement) {
        psiElement = (PsiElement)elements[0];
      }
      else if (elements[0] instanceof PackageElement) {
        psiElement = ((PackageElement)elements[0]).getPackage();
      }
      else {
        return null;
      }
      return psiElement != null && psiElement.isValid() ? psiElement : null;
    }
    if (DataConstantsEx.PSI_ELEMENT_ARRAY.equals(dataId)) {
      final PsiElement[] elements = getSelectedPsiElements();
      if (elements == null) {
        return null;
      }
      ArrayList<PsiElement> result = new ArrayList<PsiElement>();
      for (PsiElement element : elements) {
        if (element.isValid()) {
          result.add(element);
        }
      }
      return result.isEmpty() ? null : result.toArray(new PsiElement[result.size()]);
    }

    if (DataConstants.IDE_VIEW.equals(dataId)) {
      return myIdeView;
    }

    if (DataConstantsEx.TARGET_PSI_ELEMENT.equals(dataId)) {
      return null;
    }

    if (DataConstants.MODULE_CONTEXT.equals(dataId)) {
      Module[] selected = getSelectedModules();
      return selected != null && selected.length == 1 ? selected[0] : null;
    }
    if (DataConstants.MODULE_CONTEXT_ARRAY.equals(dataId)) {
      return getSelectedModules();
    }

    if (DataConstantsEx.DELETE_ELEMENT_PROVIDER.equals(dataId)) {
      final Object[] elements = getSelectedNodeElements();
      return elements != null && elements.length >= 1 && elements[0] instanceof Module
             ? myDeleteModuleProvider
             : myDeletePSIElementProvider;

    }
    if (DataConstantsEx.MODULE_GROUP_ARRAY.equals(dataId)) {
      final List<ModuleGroup> selectedElements = getSelectedElements(ModuleGroup.class);
      return selectedElements.isEmpty() ? null : selectedElements.toArray(new ModuleGroup[selectedElements.size()]);
    }
    if (DataConstantsEx.LIBRARY_GROUP_ARRAY.equals(dataId)) {
      final List<LibraryGroupElement> selectedElements = getSelectedElements(LibraryGroupElement.class);
      return selectedElements.isEmpty() ? null : selectedElements.toArray(new LibraryGroupElement[selectedElements.size()]);
    }
    if (DataConstantsEx.NAMED_LIBRARY_ARRAY.equals(dataId)) {
      final List<NamedLibraryElement> selectedElements = getSelectedElements(NamedLibraryElement.class);
      return selectedElements.isEmpty() ? null : selectedElements.toArray(new NamedLibraryElement[selectedElements.size()]);
    }
    if (CONTEXT_FAVORITES_ROOTS.equals(dataId)) {
      List<FavoritesTreeNodeDescriptor> result = new ArrayList<FavoritesTreeNodeDescriptor>();
      FavoritesTreeNodeDescriptor[] selectedNodeDescriptors = getSelectedNodeDescriptors();
      for (FavoritesTreeNodeDescriptor selectedNodeDescriptor : selectedNodeDescriptors) {
        FavoritesTreeNodeDescriptor root = selectedNodeDescriptor.getFavoritesRoot();
        if (root != null && !(root.getElement().getValue() instanceof String)) {
          result.add(root);
        }
      }
      return result.toArray(new FavoritesTreeNodeDescriptor[result.size()]);
    }
    if (FAVORITES_LIST_NAME.equals(dataId)) {
      return myListName;
    }
    FavoritesTreeNodeDescriptor[] descriptors = getSelectedNodeDescriptors();
    if (descriptors.length > 0) {
      List<AbstractTreeNode> nodes = new ArrayList<AbstractTreeNode>();
      for (FavoritesTreeNodeDescriptor descriptor : descriptors) {
        nodes.add(descriptor.getElement());
      }
      return myFavoritesTreeStructure.getDataFromProviders(nodes, dataId);
    }
    return null;
  }

  private <T> List<T> getSelectedElements(Class<T> klass) {
    final Object[] elements = getSelectedNodeElements();
    ArrayList<T> result = new ArrayList<T>();
    if (elements == null) {
      return result;
    }
    for (Object element : elements) {
      if (element == null) continue;
      if (klass.isAssignableFrom(element.getClass())) {
        result.add((T)element);
      }
    }
    return result;
  }

  private Module[] getSelectedModules() {
    final Object[] elements = getSelectedNodeElements();
    if (elements == null) {
      return null;
    }
    ArrayList<Module> result = new ArrayList<Module>();
    for (Object element : elements) {
      if (element instanceof Module) {
        result.add((Module)element);
      }
      else if (element instanceof ModuleGroup) {
        result.addAll(((ModuleGroup)element).modulesInGroup(myProject, true));
      }
    }

    return result.isEmpty() ? null : result.toArray(new Module[result.size()]);
  }

  private Object[] getSelectedNodeElements() {
    final FavoritesTreeNodeDescriptor[] selectedNodeDescriptors = getSelectedNodeDescriptors();
    ArrayList<Object> result = new ArrayList<Object>();
    for (FavoritesTreeNodeDescriptor selectedNodeDescriptor : selectedNodeDescriptors) {
      if (selectedNodeDescriptor != null) {
        Object value = selectedNodeDescriptor.getElement().getValue();
        if (value instanceof SmartPsiElementPointer) {
          value = ((SmartPsiElementPointer)value).getElement();
        }
        result.add(value);
      }
    }
    return result.toArray(new Object[result.size()]);
  }

  @NotNull
  public FavoritesTreeNodeDescriptor[] getSelectedNodeDescriptors() {
    TreePath[] path = myTree.getSelectionPaths();
    if (path == null) {
      return FavoritesTreeNodeDescriptor.EMPTY_ARRAY;
    }
    ArrayList<FavoritesTreeNodeDescriptor> result = new ArrayList<FavoritesTreeNodeDescriptor>();
    for (TreePath treePath : path) {
      DefaultMutableTreeNode lastPathNode = (DefaultMutableTreeNode)treePath.getLastPathComponent();
      Object userObject = lastPathNode.getUserObject();
      if (!(userObject instanceof FavoritesTreeNodeDescriptor)) {
        continue;
      }
      FavoritesTreeNodeDescriptor treeNodeDescriptor = (FavoritesTreeNodeDescriptor)userObject;
      result.add(treeNodeDescriptor);
    }
    return result.toArray(new FavoritesTreeNodeDescriptor[result.size()]);
  }

  public static String getQualifiedName(final VirtualFile file) {
    return file.getPresentableUrl();
  }

  public FavoritesViewTreeBuilder getBuilder() {
    return myBuilder;
  }

  private final class MyDeletePSIElementProvider implements DeleteProvider {
    public boolean canDeleteElement(DataContext dataContext) {
      final PsiElement[] elements = getElementsToDelete();
      return DeleteHandler.shouldEnableDeleteAction(elements);
    }

    public void deleteElement(DataContext dataContext) {
      List<PsiElement> allElements = Arrays.asList(getElementsToDelete());
      List<PsiElement> validElements = new ArrayList<PsiElement>();
      for (PsiElement psiElement : allElements) {
        if (psiElement != null && psiElement.isValid()) validElements.add(psiElement);
      }
      final PsiElement[] elements = validElements.toArray(new PsiElement[validElements.size()]);

      LocalHistoryAction a = LocalHistory.startAction(myProject, IdeBundle.message("progress.deleting"));
      try {
        DeleteHandler.deletePsiElement(elements, myProject);
      }
      finally {
        a.finish();
      }
    }

    private PsiElement[] getElementsToDelete() {
      ArrayList<PsiElement> result = new ArrayList<PsiElement>();
      Object[] elements = getSelectedNodeElements();
      for (int idx = 0; elements != null && idx < elements.length; idx++) {
        if (elements[idx] instanceof PsiElement) {
          final PsiElement element = (PsiElement)elements[idx];
          result.add(element);
          if (element instanceof PsiDirectory) {
            final VirtualFile virtualFile = ((PsiDirectory)element).getVirtualFile();
            final String path = virtualFile.getPath();
            if (path.endsWith(JarFileSystem.JAR_SEPARATOR)) { // if is jar-file root
              final VirtualFile vFile =
                LocalFileSystem.getInstance().findFileByPath(path.substring(0, path.length() - JarFileSystem.JAR_SEPARATOR.length()));
              if (vFile != null) {
                final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(vFile);
                if (psiFile != null) {
                  elements[idx] = psiFile;
                }
              }
            }
          }
        }
      }

      return result.toArray(new PsiElement[result.size()]);
    }
  }

  private final class MyIdeView implements IdeView {
    public void selectElement(final PsiElement element) {
      if (element != null) {
        final boolean isDirectory = element instanceof PsiDirectory;
        if (!isDirectory) {
          Editor editor = EditorHelper.openInEditor(element);
          if (editor != null) {
            ToolWindowManager.getInstance(myProject).activateEditorComponent();
          }
        }
      }
    }

    private PsiDirectory getDirectory() {
      if (myBuilder == null) return null;
      final FavoritesTreeNodeDescriptor[] selectedNodeDescriptors = getSelectedNodeDescriptors();
      if (selectedNodeDescriptors.length != 1) return null;
      final FavoritesTreeNodeDescriptor currentDescriptor = selectedNodeDescriptors[0];
      if (currentDescriptor != null) {
        if (currentDescriptor.getElement() != null) {
          final AbstractTreeNode currentNode = currentDescriptor.getElement();
          if (currentNode.getValue() instanceof PsiDirectory) {
            return (PsiDirectory)currentNode.getValue();
          }
        }
        final NodeDescriptor parentDescriptor = currentDescriptor.getParentDescriptor();
        if (parentDescriptor != null) {
          final Object parentElement = parentDescriptor.getElement();
          if (parentElement instanceof AbstractTreeNode) {
            final AbstractTreeNode parentNode = (AbstractTreeNode)parentElement;
            final Object directory = parentNode.getValue();
            if (directory instanceof PsiDirectory) {
              return (PsiDirectory)directory;
            }
          }
        }
      }

      return null;
    }

    public PsiDirectory[] getDirectories() {
      PsiDirectory directory = getDirectory();
      return directory == null ? PsiDirectory.EMPTY_ARRAY : new PsiDirectory[]{directory};
    }

    public PsiDirectory getOrChooseDirectory() {
      return PackageUtil.getOrChooseDirectory(this);
    }
  }
}