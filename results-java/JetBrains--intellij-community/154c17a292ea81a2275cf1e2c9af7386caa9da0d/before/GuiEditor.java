package com.intellij.uiDesigner.designSurface;

import com.intellij.ide.DeleteProvider;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.DataConstantsEx;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ex.IdeFocusTraversalPolicy;
import com.intellij.psi.*;
import com.intellij.uiDesigner.*;
import com.intellij.uiDesigner.compiler.Utils;
import com.intellij.uiDesigner.componentTree.ComponentPtr;
import com.intellij.uiDesigner.componentTree.ComponentSelectionListener;
import com.intellij.uiDesigner.componentTree.ComponentTree;
import com.intellij.uiDesigner.componentTree.ComponentTreeBuilder;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Util;
import com.intellij.uiDesigner.lw.CompiledClassPropertiesProvider;
import com.intellij.uiDesigner.lw.IComponent;
import com.intellij.uiDesigner.lw.IProperty;
import com.intellij.uiDesigner.lw.LwRootContainer;
import com.intellij.uiDesigner.palette.Palette;
import com.intellij.uiDesigner.palette.PaletteWindow;
import com.intellij.uiDesigner.propertyInspector.PropertyInspector;
import com.intellij.uiDesigner.propertyInspector.properties.IntroStringProperty;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DnDConstants;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Locale;

/**
 * <code>GuiEditor</code> is a panel with border layout. It has palette at the north,
 * tree of component with property editor at the west and editor area at the center.
 * This editor area contains internal component where user edit the UI.
 *
 * @author Anton Katilin
 * @author Vladimir Kondratyev
 */
public final class GuiEditor extends JPanel implements DataProvider {
  private static final Logger LOG = Logger.getInstance("#com.intellij.uiDesigner.GuiEditor");

  @NotNull private final Module myModule;
  @NotNull private final VirtualFile myFile;

  /**
   * for debug purposes
   */
  private Exception myWhere;

  /**
   * All component are on this layer
   */
  private static final Integer LAYER_COMPONENT = JLayeredPane.DEFAULT_LAYER;
  /**
   * This layer contains all "passive" decorators such as component boundaries
   * and selection rectangle.
   */
  private static final Integer LAYER_PASSIVE_DECORATION = JLayeredPane.POPUP_LAYER;
  /**
   * We show (and move) dragged component at this layer
   */
  private static final Integer LAYER_DND = JLayeredPane.DRAG_LAYER;
  /**
   * This is the topmost layer. It gets and redispatch all incoming events
   */
  private static final Integer LAYER_GLASS = new Integer(JLayeredPane.DRAG_LAYER.intValue() + 100);
  /**
   * This layer contains all "active" decorators. This layer should be over
   * LAYER_GLASS because active decorators must get AWT events to work correctly.
   */
  private static final Integer LAYER_ACTIVE_DECORATION = new Integer(LAYER_GLASS.intValue() + 100);
  /**
   * This layer contains all inplace editors.
   */
  private static final Integer LAYER_INPLACE_EDITING = new Integer(LAYER_ACTIVE_DECORATION.intValue() + 100);

  private final EventListenerList myListenerList;
  /**
   * we have to store document here but not file because there can be a situation when
   * document we added listener to has been disposed, and remove listener will be applied to
   * a new document (got by file) -> assertion (see SCR 14143)
   */
  private final Document myDocument;

  final MainProcessor myProcessor;
  /**
   * This layered pane contains all layers to lay components out and to
   * show all necessary decoration items
   */
  @NotNull private final MyLayeredPane myLayeredPane;
  /**
   * The component which represents decoration layer. All passive
   * decorators are on this layer.
   */
  private final PassiveDecorationLayer myDecorationLayer;
  /**
   * The component which represents layer where located all dragged
   * components
   */
  private final DragLayer myDragLayer;
  /**
   * This layer contains all inplace editors
   */
  private final InplaceEditingLayer myInplaceEditingLayer;
  /**
   * Brings functionality to "DEL" button
   */
  private final MyDeleteProvider myDeleteProvider;
  /**
   * Rerun error analizer
   */
  private final MyPsiTreeChangeListener myPsiTreeChangeListener;

  private RadRootContainer myRootContainer;
  /**
   * Panel with components palette.
   */
  //@NotNull private final PalettePanel myPalettePanel;
  /**
   * GuiEditor should not react on own events. If <code>myInsideChange</code>
   * is <code>true</code> then we do not react on incoming DocumentEvent.
   */
  private boolean myInsideChange;
  @NotNull private final PropertyInspector myPropertyInspector;
  @NotNull private final ComponentTree myComponentTree;
  private final DocumentAdapter myDocumentListener;
  private final CardLayout myCardLayout;

  @NonNls
  private final static String CARD_VALID = "valid";
  @NonNls
  private final static String CARD_INVALID = "invalid";
  private final JPanel myValidCard;
  private final JPanel myInvalidCard;

  private final CutCopyPasteSupport myCutCopyPasteSupport;
  /**
   * Implementation of Crtl+W and Ctrl+Shift+W behavior
   */
  private final SelectionState mySelectionState;
  @NotNull private final GlassLayer myGlassLayer;
  private ActiveDecorationLayer myActiveDecorationLayer;

  private boolean myShowGrid = true;
  private DesignDropTargetListener myDropTargetListener;

  /**
   * @param file file to be edited
   * @throws java.lang.IllegalArgumentException
   *          if the <code>file</code>
   *          is <code>null</code> or <code>file</code> is not falid PsiFile
   */
  public GuiEditor(@NotNull final Module module, @NotNull final VirtualFile file) {
    ApplicationManager.getApplication().assertIsDispatchThread();
    LOG.assertTrue(file.isValid());

    myModule = module;
    myFile = file;

    myCutCopyPasteSupport = new CutCopyPasteSupport(this);

    myCardLayout = new CardLayout();
    setLayout(myCardLayout);

    myValidCard = new JPanel(new BorderLayout());
    myInvalidCard = createInvalidCard();
    add(myValidCard, CARD_VALID);
    add(myInvalidCard, CARD_INVALID);

    myListenerList = new EventListenerList();

    myDecorationLayer = new PassiveDecorationLayer(this);
    myDragLayer = new DragLayer(this);

    myLayeredPane = new MyLayeredPane();
    myInplaceEditingLayer = new InplaceEditingLayer(this);
    myLayeredPane.add(myInplaceEditingLayer, LAYER_INPLACE_EDITING);
    myActiveDecorationLayer = new ActiveDecorationLayer(this);
    myLayeredPane.add(myActiveDecorationLayer, LAYER_ACTIVE_DECORATION);
    myGlassLayer = new GlassLayer(this);
    myLayeredPane.add(myGlassLayer, LAYER_GLASS);
    myLayeredPane.add(myDecorationLayer, LAYER_PASSIVE_DECORATION);
    myLayeredPane.add(myDragLayer, LAYER_DND);

    myGlassLayer.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        myDecorationLayer.repaint();
      }

      public void focusLost(FocusEvent e) {
        myDecorationLayer.repaint();
      }
    });

    // Ctrl+W / Ctrl+Shift+W support
    mySelectionState = new SelectionState(this);

    // DeleteProvider
    myDeleteProvider = new MyDeleteProvider();

    // We need to synchronize GUI editor with the document
    final Alarm alarm = new Alarm();
    myDocumentListener = new DocumentAdapter() {
      public void documentChanged(final DocumentEvent e) {
        if (!myInsideChange) {
          UndoManager undoManager = UndoManager.getInstance(module.getProject());
          alarm.cancelAllRequests();
          alarm.addRequest(new MySynchronizeRequest(module, undoManager.isUndoInProgress() || undoManager.isRedoInProgress()),
                           100/*any arbitrary delay*/, ModalityState.stateForComponent(GuiEditor.this));
        }
      }
    };

    // Prepare document
    myDocument = FileDocumentManager.getInstance().getDocument(file);
    myDocument.addDocumentListener(myDocumentListener);

    // Read form from file
    readFromFile(false);

    // Tree with component hierarchy and property editor at the left
    // It's important that ComponentTree is initializing after the
    // RadRootContainer is set.
    myComponentTree = new ComponentTree(this);
    new ComponentTreeBuilder(myComponentTree, this);
    final JScrollPane scrollPane = new JScrollPane(myComponentTree);
    scrollPane.setPreferredSize(new Dimension(250, -1));

    // Splitter with hierarchy and inspector
    final Splitter splitter1 = new Splitter(true, 0.33f);
    splitter1.setFirstComponent(scrollPane);
    myPropertyInspector = new PropertyInspector(this, myComponentTree);
    splitter1.setSecondComponent(myPropertyInspector);

    final Splitter splitter2 = new Splitter(false, 0.33f);
    splitter2.setFirstComponent(splitter1);

    final JPanel mainPaneAndToolbar = new JPanel(new BorderLayout());
    mainPaneAndToolbar.add(new JScrollPane(myLayeredPane), BorderLayout.CENTER);

    splitter2.setSecondComponent(mainPaneAndToolbar);
    myValidCard.add(splitter2, BorderLayout.CENTER);

    final CancelCurrentOperationAction cancelCurrentOperationAction = new CancelCurrentOperationAction();
    cancelCurrentOperationAction.registerCustomShortcutSet(CommonShortcuts.ESCAPE, this);

    // Palette at the top
    // It is important to create palette toolbar after root container has been loaded
    /*
    myPalettePanel = new PalettePanel(this);
    mainPaneAndToolbar.add(myPalettePanel, BorderLayout.NORTH);
    */
    myProcessor = new MainProcessor(this);
    new MergeCellsToolbar(this);

    // PSI listener to restart error highlighter
    myPsiTreeChangeListener = new MyPsiTreeChangeListener();
    PsiManager.getInstance(module.getProject()).addPsiTreeChangeListener(myPsiTreeChangeListener);

    new QuickFixManagerImpl(this, myGlassLayer);

    myDropTargetListener = new DesignDropTargetListener(this);
    new DropTarget(getGlassLayer(), DnDConstants.ACTION_COPY_OR_MOVE, myDropTargetListener);
  }

  @NotNull
  public SelectionState getSelectionState() {
    return mySelectionState;
  }

  public void dispose() {
    ApplicationManager.getApplication().assertIsDispatchThread();

    if (myWhere != null) {
      LOG.error("Already disposed: old trace: ", myWhere);
      LOG.error("Already disposed: new trace: ");
    }
    else {
      myWhere = new Exception();
    }

    myDocument.removeDocumentListener(myDocumentListener);
    PsiManager.getInstance(myModule.getProject()).removePsiTreeChangeListener(myPsiTreeChangeListener);
    myPsiTreeChangeListener.dispose();
  }

  @NotNull
  public Project getProject() {
    return myModule.getProject();
  }

  @NotNull
  public Module getModule() {
    return myModule;
  }

  @NotNull
  public VirtualFile getFile() {
    return myFile;
  }

  public boolean isEditable() {
    final Document document = FileDocumentManager.getInstance().getDocument(myFile);
    return document != null && document.isWritable();
  }

  public boolean ensureEditable() {
    if (isEditable()) {
      return true;
    }
    final ReadonlyStatusHandler.OperationStatus status = ReadonlyStatusHandler.getInstance(getProject()).ensureFilesWritable(myFile);
    return !status.hasReadonlyFiles();
  }

  public void refresh() {
    refreshImpl(myRootContainer);
    myRootContainer.getDelegee().revalidate();
    repaintLayeredPane();

    // Collect errors
    ErrorAnalyzer.analyzeErrors(this, myRootContainer);
  }

  public void refreshAndSave(final boolean forceSync) {
    // Update property inspector
    if (myPropertyInspector != null) { // null when we invoke refresh from constructor
      myPropertyInspector.synchWithTree(forceSync);
    }

    refresh();
    saveToFile();
  }

  /**
   * Refresh error markers in all components
   */
  public void refreshErrors() {
    // Collect errors
    ErrorAnalyzer.analyzeErrors(this, myRootContainer);
    if (isShowing()) {
      //  ComponentTree
      myComponentTree.hideIntentionHint();
      myComponentTree.updateIntentionHintVisibility();
      myComponentTree.repaint(myComponentTree.getVisibleRect());

      // PropertyInspector
      myPropertyInspector.hideIntentionHint();
      myPropertyInspector.updateIntentionHintVisibility();
      myPropertyInspector.repaint(myPropertyInspector.getVisibleRect());
    }
  }

  private void refreshImpl(final RadComponent component) {
    if (component.getParent() != null) {
      final Dimension size = component.getSize();
      final int oldWidth = size.width;
      final int oldHeight = size.height;
      Util.adjustSize(component.getDelegee(), component.getConstraints(), size);

      if (oldWidth != size.width || oldHeight != size.height) {
        if (component.getParent().isXY()) {
          component.setSize(size);
        }
        component.getDelegee().invalidate();
      }
    }

    if (component instanceof RadContainer) {
      final RadContainer container = (RadContainer)component;
      for (int i = container.getComponentCount() - 1; i >= 0; i--) {
        refreshImpl(container.getComponent(i));
      }
    }
  }

  public Object getData(final String dataId) {
    // Standard Swing cut/copy/paste actions should work if user is editing something inside property inspector
    if (myPropertyInspector.isEditing()) {
      return null;
    }

    if (DataConstantsEx.DELETE_ELEMENT_PROVIDER.equals(dataId)) {
      return myDeleteProvider;
    }

    if (
      DataConstantsEx.COPY_PROVIDER.equals(dataId) ||
      DataConstantsEx.CUT_PROVIDER.equals(dataId) ||
      DataConstantsEx.PASTE_PROVIDER.equals(dataId)
    ) {
      return myCutCopyPasteSupport;
    }

    return null;
  }

  private JPanel createInvalidCard() {
    final JPanel panel = new JPanel(new GridBagLayout());
    panel.add(new JLabel(UIDesignerBundle.message("error.form.file.is.invalid")),
              new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    return panel;
  }

  /**
   * @return the component which represents DnD layer. All currently
   *         dragged (moved) component are on this layer.
   */
  public DragLayer getDragLayer() {
    return myDragLayer;
  }

  /**
   * @return the topmost <code>UiConainer</code> which in the root of
   *         component hierarchy. This method never returns <code>null</code>.
   */
  @NotNull
  public RadRootContainer getRootContainer() {
    return myRootContainer;
  }

  /**
   * Fires event that selection changes
   */
  public void fireSelectedComponentChanged() {
    final ComponentSelectionListener[] listeners = myListenerList.getListeners(ComponentSelectionListener.class);
    for (ComponentSelectionListener listener : listeners) {
      listener.selectedComponentChanged(this);
    }
  }

  private void fireHierarchyChanged() {
    final HierarchyChangeListener[] listeners = myListenerList.getListeners(HierarchyChangeListener.class);
    for(final HierarchyChangeListener listener : listeners) {
      listener.hierarchyChanged();
    }
  }

  @NotNull
  GlassLayer getGlassLayer() {
    return myGlassLayer;
  }

  /**
   * @return the component which represents layer with active decorators
   * such as grid edit controls, inplace editors, etc.
   */
  public InplaceEditingLayer getInplaceEditingLayer() {
    return myInplaceEditingLayer;
  }

  @NotNull
  public JLayeredPane getLayeredPane() {
    return myLayeredPane;
  }

  public void repaintLayeredPane() {
    myLayeredPane.repaint();
  }

  /**
   * Adds specified selection listener. This listener gets notification each time
   * the selection in the component the changes.
   */
  public void addComponentSelectionListener(final ComponentSelectionListener l) {
    myListenerList.add(ComponentSelectionListener.class, l);
  }

  /**
   * Removes specified selection listener
   */
  public void removeComponentSelectionLsistener(final ComponentSelectionListener l) {
    myListenerList.remove(ComponentSelectionListener.class, l);
  }

  /**
   * Adds specified hierarchy change listener
   */
  public void addHierarchyChangeListener(final HierarchyChangeListener l) {
    LOG.assertTrue(l != null);
    myListenerList.add(HierarchyChangeListener.class, l);
  }

  /**
   * Removes specified hierarchy change listener
   */
  public void removeHierarchyChangeListener(final HierarchyChangeListener l) {
    LOG.assertTrue(l != null);
    myListenerList.remove(HierarchyChangeListener.class, l);
  }

  private void saveToFile() {
    CommandProcessor.getInstance().executeCommand(myModule.getProject(), new Runnable() {
      public void run() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            myInsideChange = true;
            try {
              final XmlWriter writer = new XmlWriter();
              getRootContainer().write(writer);
              final String newText = writer.getText();
              final String oldText = myDocument.getText();

              try {
                final ReplaceInfo replaceInfo = findFragmentToChange(oldText, newText);
                if (replaceInfo.getStartOffset() == -1) {
                  // do nothing - texts are equal
                }
                else {
                  myDocument.replaceString(replaceInfo.getStartOffset(), replaceInfo.getEndOffset(), replaceInfo.getReplacement());
                }
              }
              catch (Exception e) {
                LOG.error(e);
                myDocument.replaceString(0, oldText.length(), newText);
              }
            }
            finally {
              myInsideChange = false;
            }
          }
        });
      }
    }, null, null);

    fireHierarchyChanged();
  }

  public ActiveDecorationLayer getActiveDecorationLayer() {
    return myActiveDecorationLayer;
  }

  public void setStringDescriptorLocale(final Locale locale) {
    myRootContainer.setStringDescriptorLocale(locale);
    refreshProperties();
    refreshErrors();   // this also has the nice side-effect of refreshing the component tree
  }

  public Locale getStringDescriptorLocale() {
    return myRootContainer.getStringDescriptorLocale();
  }

  private void refreshProperties() {
    FormEditingUtil.iterate(myRootContainer, new FormEditingUtil.ComponentVisitor() {
      public boolean visit(final IComponent component) {
        final RadComponent radComponent = (RadComponent)component;
        for(IProperty prop: component.getModifiedProperties()) {
          if (prop instanceof IntroStringProperty) {
            IntroStringProperty strProp = (IntroStringProperty) prop;
            strProp.refreshValue(radComponent);
          }
        }

        if (component instanceof RadContainer) {
          ((RadContainer) component).updateBorder();
        }

        if (component.getParentContainer() instanceof RadTabbedPane) {
          ((RadTabbedPane) component.getParentContainer()).refreshChildTitle(radComponent);
        }

        return true;
      }
    });
  }

  public static final class ReplaceInfo {
    private final int myStartOffset;
    private final int myEndOffset;
    private final String myReplacement;

    public ReplaceInfo(final int startOffset, final int endOffset, final String replacement) {
      myStartOffset = startOffset;
      myEndOffset = endOffset;
      myReplacement = replacement;
    }

    public int getStartOffset() {
      return myStartOffset;
    }

    public int getEndOffset() {
      return myEndOffset;
    }

    public String getReplacement() {
      return myReplacement;
    }
  }

  public static ReplaceInfo findFragmentToChange(final String oldText, final String newText) {
    if (oldText.equals(newText)) {
      return new ReplaceInfo(-1, -1, null);
    }

    final int oldLength = oldText.length();
    final int newLength = newText.length();

    int startOffset = 0;
    while (
      startOffset < oldLength && startOffset < newLength &&
      oldText.charAt(startOffset) == newText.charAt(startOffset)
      ) {
      startOffset++;
    }

    int endOffset = oldLength;
    while (true) {
      if (endOffset <= startOffset) {
        break;
      }
      final int idxInNew = newLength - (oldLength - endOffset) - 1;
      if (idxInNew < startOffset) {
        break;
      }

      final char c1 = oldText.charAt(endOffset - 1);
      final char c2 = newText.charAt(idxInNew);
      if (c1 != c2) {
        break;
      }
      endOffset--;
    }

    return new ReplaceInfo(startOffset, endOffset, newText.substring(startOffset, newLength - (oldLength - endOffset)));
  }

  /**
   * @param rootContainer new container to be set as a root.
   */
  private void setRootContainer(@NotNull final RadRootContainer rootContainer) {
    if (myRootContainer != null) {
      myLayeredPane.remove(myRootContainer.getDelegee());
    }
    myRootContainer = rootContainer;
    setDesignTimeInsets(2);
    myLayeredPane.add(myRootContainer.getDelegee(), LAYER_COMPONENT);

    fireHierarchyChanged();
  }

  public void setDesignTimeInsets(final int insets) {
    Integer oldInsets = (Integer) myRootContainer.getDelegee().getClientProperty(GridLayoutManager.DESIGN_TIME_INSETS);
    if (oldInsets == null || oldInsets.intValue() != insets) {
      myRootContainer.getDelegee().putClientProperty(GridLayoutManager.DESIGN_TIME_INSETS, new Integer(insets));
      revalidateRecursive(myRootContainer.getDelegee());
    }
  }

  private void revalidateRecursive(final JComponent component) {
    for(Component child: component.getComponents()) {
      if (child instanceof JComponent) {
        revalidateRecursive((JComponent)child);
      }
    }
    component.revalidate();
    component.repaint();
  }

  /**
   * Creates and sets new <code>RadRootContainer</code>
   * @param keepSelection if true, the GUI designer tries to preserve the selection state after reload.
   */
  private void readFromFile(final boolean keepSelection) {
    try {
      ComponentPtr[] selection = null;
      if (keepSelection) {
        selection = SelectionState.getSelection(this);
      }

      final String text = myDocument.getText();

      final ClassLoader classLoader = LoaderFactory.getInstance(myModule.getProject()).getLoader(myFile);

      final LwRootContainer rootContainer = Utils.getRootContainer(text, new CompiledClassPropertiesProvider(classLoader));
      final RadRootContainer container = XmlReader.createRoot(myModule, rootContainer, classLoader);
      setRootContainer(container);
      if (keepSelection) {
        SelectionState.restoreSelection(this, selection);
      }
      myCardLayout.show(this, CARD_VALID);
      refresh();
    }
    catch (final Exception exc) {
      LOG.info(exc);
      // setting fictive container
      setRootContainer(new RadRootContainer(myModule, JPanel.class, "0"));
      myCardLayout.show(this, CARD_INVALID);
      repaint();
    }
  }

  public JComponent getPreferredFocusedComponent() {
    if (myValidCard.isVisible()) {
      return IdeFocusTraversalPolicy.getPreferredFocusedComponent(myValidCard);
    }
    else {
      return myInvalidCard;
    }
  }

  public PaletteWindow getPaletteWindow() {
    return Palette.getInstance(getProject()).getPaletteWindow();
  }

  @NotNull
  public ComponentTree getComponentTree() {
    return myComponentTree;
  }

  @NotNull
  public PropertyInspector getPropertyInspector() {
    return myPropertyInspector;
  }

  /**
   * @return id
   */
  public String generateId() {
    while (true) {
      final String id = Integer.toString((int)(Math.random() * 1024 * 1024), 16);
      if (!idAlreadyExist(id, getRootContainer())) {
        return id;
      }
    }
  }

  private static boolean idAlreadyExist(final String id, final RadComponent component) {
    if (id.equals(component.getId())) {
      return true;
    }
    if (component instanceof RadContainer) {
      final RadContainer container = (RadContainer)component;
      for (int i = 0; i < container.getComponentCount(); i++) {
        if (idAlreadyExist(id, container.getComponent(i))) {
          return true;
        }
      }
    }
    return false;
  }

  public static void repaintLayeredPane(final RadComponent component) {
    final GuiEditor uiEditor = (GuiEditor)SwingUtilities.getAncestorOfClass(GuiEditor.class, component.getDelegee());
    if (uiEditor != null) {
      uiEditor.repaintLayeredPane();
    }
  }

  public boolean isShowGrid() {
    return myShowGrid;
  }

  public void setShowGrid(final boolean showGrid) {
    if (myShowGrid != showGrid) {
      myShowGrid = showGrid;
      repaint();
    }
  }

  public DesignDropTargetListener getDropTargetListener() {
    return myDropTargetListener;
  }

  private final class MyLayeredPane extends JLayeredPane {
    /**
     * All components allocate whole pane's area.
     */
    public void doLayout() {
      for (int i = getComponentCount() - 1; i >= 0; i--) {
        final Component component = getComponent(i);
        component.setBounds(0, 0, getWidth(), getHeight());
      }
    }

    public Dimension getMinimumSize() {
      return getPreferredSize();
    }

    public Dimension getPreferredSize() {
      // make sure all components fit
      int width = 0;
      int height = 0;
      for (int i = 0; i < myRootContainer.getComponentCount(); i++) {
        final RadComponent component = myRootContainer.getComponent(i);
        width = Math.max(width, component.getX() + component.getWidth());
        height = Math.max(height, component.getY() + component.getHeight());
      }

      width += 30;
      height += 30;

      return new Dimension(width, height);
    }
  }

  /**
   * Action works only if we are not editing something in the property inspector
   */
  private final class CancelCurrentOperationAction extends AnAction {
    public void actionPerformed(final AnActionEvent e) {
      myProcessor.cancelOperation();
    }

    public void update(final AnActionEvent e) {
      e.getPresentation().setEnabled(!myPropertyInspector.isEditing());
    }
  }

  /**
   * Allows "DEL" button to work through the standard mechanism
   */
  private final class MyDeleteProvider implements DeleteProvider {
    public void deleteElement(final DataContext dataContext) {
      if (!GuiEditor.this.ensureEditable()) {
        return;
      }
      FormEditingUtil.deleteSelection(GuiEditor.this);
    }

    public boolean canDeleteElement(final DataContext dataContext) {
      return
        !myPropertyInspector.isEditing() &&
        !myInplaceEditingLayer.isEditing() &&
        FormEditingUtil.canDeleteSelection(GuiEditor.this);
    }
  }

  /**
   * Listens PSI event and update error highlighting in the UI editor
   */
  private final class MyPsiTreeChangeListener extends PsiTreeChangeAdapter implements Runnable {
    private final Alarm myAlarm;
    private final MyRefreshPropertiesRequest myRefreshPropertiesRequest = new MyRefreshPropertiesRequest();

    public MyPsiTreeChangeListener() {
      myAlarm = new Alarm();
    }

    /**
     * Cancels all pending update requests. You have to cancel all pending requests
     * to not access to closed project.
     */
    public void dispose() {
      myAlarm.cancelAllRequests();
    }

    public void childAdded(final PsiTreeChangeEvent event) {
      handleEvent(event);
    }

    public void childMoved(final PsiTreeChangeEvent event) {
      handleEvent(event);
    }

    public void childrenChanged(final PsiTreeChangeEvent event) {
      handleEvent(event);
    }

    public void childRemoved(PsiTreeChangeEvent event) {
      handleEvent(event);
    }

    public void childReplaced(PsiTreeChangeEvent event) {
      handleEvent(event);
    }

    public void propertyChanged(final PsiTreeChangeEvent event) {
      if (PsiTreeChangeEvent.PROP_ROOTS.equals(event.getPropertyName())) {
        handleEvent(event);
      }
    }

    private void handleEvent(final PsiTreeChangeEvent event) {
      if (event.getParent() != null && event.getParent().getContainingFile() instanceof PropertiesFile) {
        myAlarm.cancelRequest(myRefreshPropertiesRequest);
        myAlarm.addRequest(myRefreshPropertiesRequest, 500, ModalityState.stateForComponent(GuiEditor.this));
      }
      else {
        myAlarm.cancelRequest(this);
        myAlarm.addRequest(this, 2500, ModalityState.stateForComponent(GuiEditor.this));
      }
    }

    /**
     * Restarts error analyzer
     */
    public void run() {
      final String classToBind = myRootContainer.getClassToBind();
      if (classToBind == null) {
        return;
      }
      final PsiClass aClass = FormEditingUtil.findClassToBind(myModule, classToBind);
      if (aClass != null) {
        refreshErrors();
      }
    }
  }

  private class MySynchronizeRequest implements Runnable {
    private final Module myModule;
    private final boolean myKeepSelection;

    public MySynchronizeRequest(final Module module, final boolean keepSelection) {
      myModule = module;
      myKeepSelection = keepSelection;
    }

    public void run() {
      PsiDocumentManager.getInstance(myModule.getProject()).commitDocument(myDocument);
      readFromFile(myKeepSelection);
    }
  }

  private class MyRefreshPropertiesRequest implements Runnable {
    public void run() {
      refreshProperties();
    }
  }
}