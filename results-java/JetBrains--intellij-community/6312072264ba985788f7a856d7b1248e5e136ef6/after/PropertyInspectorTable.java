package com.intellij.uiDesigner.propertyInspector;

import com.intellij.codeInsight.daemon.impl.SeverityRegistrar;
import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ex.IdeFocusTraversalPolicy;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TableUtil;
import com.intellij.uiDesigner.ErrorAnalyzer;
import com.intellij.uiDesigner.ErrorInfo;
import com.intellij.uiDesigner.Properties;
import com.intellij.uiDesigner.UIDesignerBundle;
import com.intellij.uiDesigner.actions.ShowJavadocAction;
import com.intellij.uiDesigner.componentTree.ComponentTree;
import com.intellij.uiDesigner.designSurface.GuiEditor;
import com.intellij.uiDesigner.palette.Palette;
import com.intellij.uiDesigner.propertyInspector.properties.*;
import com.intellij.uiDesigner.radComponents.*;
import com.intellij.util.ui.EmptyIcon;
import com.intellij.util.ui.Table;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.TableUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author Anton Katilin
 * @author Vladimir Kondratyev
 */
public final class PropertyInspectorTable extends Table implements DataProvider{
  private static final Logger LOG = Logger.getInstance("#intellij.uiDesigner.propertyInspector.PropertyInspectorTable");

  private static final Color SYNTETIC_PROPERTY_BACKGROUND = new Color(230,230,230);
  private static final Color SYNTETIC_SUBPROPERTY_BACKGROUND = new Color(240,240,240);

  private final ComponentTree myComponentTree;
  private final ArrayList<Property> myProperties;
  private final MyModel myModel;
  private final MyCompositeTableCellRenderer myCellRenderer;
  private final MyCellEditor myCellEditor;
  private GuiEditor myEditor;
  /**
   * This listener gets notifications from current property editor
   */
  private final MyPropertyEditorListener myPropertyEditorListener;
  /**
   * Updates UIs of synthetic properties
   */
  private final MyLafManagerListener myLafManagerListener;
  /**
   * This is property exists in this map then it's expanded.
   * It means that its children is visible.
   */
  private final HashSet<String> myExpandedProperties;
  /**
   * Component to be edited
   */
  private RadComponent myComponent;
  /**
   * If true then inspector will show "expert" properties
   */
  private boolean myShowExpertProperties;

  private Map<HighlightSeverity, SimpleTextAttributes> myHighlightAttributes = new HashMap<HighlightSeverity, SimpleTextAttributes>();
  private Map<HighlightSeverity, SimpleTextAttributes> myModifiedHighlightAttributes = new HashMap<HighlightSeverity, SimpleTextAttributes>();

  private final ClassToBindProperty myClassToBindProperty;
  private final BindingProperty myBindingProperty;
  private final BorderProperty myBorderProperty;
  private final LayoutManagerProperty myLayoutManagerProperty = new LayoutManagerProperty();
  private final ButtonGroupProperty myButtonGroupProperty = new ButtonGroupProperty();

  private boolean myInsideSynch;
  private final Project myProject;

  PropertyInspectorTable(Project project, @NotNull final ComponentTree componentTree) {
    myProject = project;
    myClassToBindProperty = new ClassToBindProperty(project);
    myBindingProperty = new BindingProperty(project);
    myBorderProperty = new BorderProperty(project);

    myPropertyEditorListener = new MyPropertyEditorListener();
    myLafManagerListener = new MyLafManagerListener();
    myComponentTree=componentTree;
    myProperties = new ArrayList<Property>();
    myExpandedProperties = new HashSet<String>();
    myModel = new MyModel();
    setModel(myModel);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    myCellRenderer = new MyCompositeTableCellRenderer();
    myCellEditor = new MyCellEditor();

    addMouseListener(new MyMouseListener());

    final AnAction quickJavadocAction = ActionManager.getInstance().getAction(IdeActions.ACTION_QUICK_JAVADOC);
    new ShowJavadocAction().registerCustomShortcutSet(
      quickJavadocAction.getShortcutSet(), this
    );

    // Popup menu
    PopupHandler.installPopupHandler(
      this,
      (ActionGroup)ActionManager.getInstance().getAction(IdeActions.GROUP_GUI_DESIGNER_PROPERTY_INSPECTOR_POPUP),
      ActionPlaces.GUI_DESIGNER_PROPERTY_INSPECTOR_POPUP, ActionManager.getInstance());
  }

  public void setEditor(final GuiEditor editor) {
    finishEditing();
    myEditor = editor;
    if (myEditor == null) {
      myComponent = null;
      myProperties.clear();
      myModel.fireTableDataChanged();
    }
  }

  /**
   * @return currently selected {@link IntrospectedProperty} or <code>null</code>
   * if nothing selected or synthetic property is selected.
   */
  @Nullable
  public IntrospectedProperty getSelectedIntrospectedProperty(){
    Property property = getSelectedProperty();
    if (property == null || !(property instanceof IntrospectedProperty)) {
      return null;
    }

    return (IntrospectedProperty)property;
  }

  @Nullable public Property getSelectedProperty() {
    final int selectedRow = getSelectedRow();
    if(selectedRow < 0 || selectedRow >= getRowCount()){
      return null;
    }

    final Module module = myEditor.getModule();

    final PsiClass aClass = PsiManager.getInstance(module.getProject()).findClass(
      myComponent.getComponentClassName(),
      GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
    );

    if (aClass == null){
      return null;
    }

    return myProperties.get(selectedRow);
  }

  /**
   * @return {@link PsiClass} of the component which properties are displayed inside the inspector
   */
  public PsiClass getComponentClass(){
    final Module module = myEditor.getModule();
    final PsiClass aClass = PsiManager.getInstance(module.getProject()).findClass(
      myComponent.getComponentClassName(),
      GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
    );
    return aClass;
  }

  public Object getData(final String dataId) {
    if(getClass().getName().equals(dataId)){
      return this;
    }
    else if(DataConstants.PSI_ELEMENT.equals(dataId)){
      final IntrospectedProperty introspectedProperty = getSelectedIntrospectedProperty();
      if(introspectedProperty == null){
        return null;
      }
      final PsiClass aClass = getComponentClass();
      if(aClass == null){
        return null;
      }

      final PsiMethod getter = PropertyUtil.findPropertyGetter(aClass, introspectedProperty.getName(), false, true);
      if(getter != null){
        return getter;
      }

      final PsiMethod setter = PropertyUtil.findPropertySetter(aClass, introspectedProperty.getName(), false, true);
      return setter;
    }
    else if (DataConstants.PSI_FILE.equals(dataId) && myEditor != null) {
      return PsiManager.getInstance(myEditor.getProject()).findFile(myEditor.getFile());
    }
    else if (GuiEditor.class.getName().equals(dataId)) {
      return myEditor;
    }
    else {
      return null;
    }
  }

  /**
   * Sets whenther "expert" properties are shown or not
   */
  void setShowExpertProperties(final boolean showExpertProperties){
    if(myShowExpertProperties == showExpertProperties){
      return;
    }
    myShowExpertProperties = showExpertProperties;
    if (myEditor != null) {
      synchWithTree(true);
    }
  }

  public void addNotify() {
    super.addNotify();
    LafManager.getInstance().addLafManagerListener(myLafManagerListener);
  }

  public void removeNotify() {
    LafManager.getInstance().removeLafManagerListener(myLafManagerListener);
    super.removeNotify();
  }

  /**
   * Standard JTable's UI has non convenient keybinding for
   * editing. Therefore we have to replace some standard actions.
   */
  public void setUI(final TableUI ui){
    super.setUI(ui);

    // Customize action and input maps
    @NonNls final ActionMap actionMap=getActionMap();
    @NonNls final InputMap focusedInputMap=getInputMap(JComponent.WHEN_FOCUSED);
    @NonNls final InputMap ancestorInputMap=getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    actionMap.put("selectPreviousRow",new MySelectPreviousRowAction());

    actionMap.put("selectNextRow",new MySelectNextRowAction());

    actionMap.put("startEditing",new MyStartEditingAction());
    focusedInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2,0),"startEditing");
    ancestorInputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_F2,0));

    actionMap.put("smartEnter",new MyEnterAction());
    focusedInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"smartEnter");
    ancestorInputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0));

    focusedInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"cancel");
    ancestorInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"cancel");

    actionMap.put("expandCurrent", new MyExpandCurrentAction(true));
    focusedInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,0),"expandCurrent");
    ancestorInputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,0));

    actionMap.put("collapseCurrent", new MyExpandCurrentAction(false));
    focusedInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT,0),"collapseCurrent");
    ancestorInputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT,0));
  }

  public void setValueAt(final Object aValue, final int row, final int column) {
    super.setValueAt(aValue, row, column);
    // We need to repaint whole inspector because change of one property
    // might causes change of another property.
    // TODO[yole]: hack!
    if (myProperties.get(row) instanceof LayoutManagerProperty) {
      synchWithTree(true);
    }
    repaint();
  }

  /**
   * Gets first selected component from ComponentTree and sets it for editing.
   * The method tries to keep selection in the list, so if new component has the property
   * which is already selected then the new value will be
   * also selected. It is very convenient.
   *
   * @param forceSynch if <code>false</code> and selected component in the ComponentTree
   * is the same as current component in the PropertyInspector then method does
   * nothing such sace. If <code>true</code> then inspector is forced to resynch.
   */
  public void synchWithTree(final boolean forceSynch){
    if (myInsideSynch) {
      return;
    }
    myInsideSynch = true;
    try {
      final RadComponent newSelectedComponent = myComponentTree.getSelectedComponent();
      if (newSelectedComponent == null) {  // possible when switching away from form editor (IDEADEV-5222)
        return;
      }

      if(!forceSynch && newSelectedComponent.equals(myComponent)){
        // Nothing changed
        return;
      }

      if (isEditing()){
        cellEditor.stopCellEditing();
      }

      // Store selected property
      final int selectedRow=getSelectedRow();
      Property selectedProperty=null;
      if(selectedRow >= 0 && selectedRow < myProperties.size()){
        selectedProperty=myProperties.get(selectedRow);
      }

      myComponent = newSelectedComponent;
      myProperties.clear();
      collectProperties(myComponent, myProperties);
      myModel.fireTableDataChanged();

      // Try to restore selection
      final ArrayList<Property> reversePath=new ArrayList<Property>(2);
      while(selectedProperty!=null){
        reversePath.add(selectedProperty);
        selectedProperty=selectedProperty.getParent();
      }
      int indexToSelect=-1;
      for(int i=reversePath.size()-1;i>=0;i--){
        final Property property=reversePath.get(i);
        int index=findPropertyByName(property.getName());
        if(index==-1 && indexToSelect!=-1){ // try to expand parent and try again
          expandProperty(indexToSelect);
          index=findPropertyByName(property.getName());
          if(index!=-1){
            indexToSelect=index;
          }else{
            break;
          }
        }else{
          indexToSelect=index;
        }
      }

      if(indexToSelect!=-1){
        getSelectionModel().setSelectionInterval(indexToSelect,indexToSelect);
      }else if(getRowCount()>0){
        // Select first row if it's impossible to restore selection
        getSelectionModel().setSelectionInterval(0,0);
      }
      TableUtil.scrollSelectionToVisible(this);
    }
    finally {
      myInsideSynch = false;
    }
  }

  /**
   * @return index of the property with specified <code>name</code>.
   * If there is no such property then the method returns <code>-1</code>.
   */
  private int findPropertyByName(final String name){
    for(int i=myProperties.size()-1;i>=0;i--){
      final Property property=myProperties.get(i);
      if(property.getName().equals(name)){
        return i;
      }
    }
    return -1;
  }

  /**
   * Populates result list with the properties available for the specified
   * component
   */
  private void collectProperties(final RadComponent component, final ArrayList<Property> result) {
    if (component instanceof RadRootContainer){
      addProperty(result, myClassToBindProperty);
    }
    else {
      if (!(component instanceof RadVSpacer || component instanceof RadHSpacer)){
        addProperty(result, myBindingProperty);
        addProperty(result, CustomCreateProperty.getInstance(myProject));
      }

      if(component instanceof RadContainer){
        RadContainer container = (RadContainer) component;
        if (container.getLayoutManager().getName() != null) {
          addProperty(result, myLayoutManagerProperty);
        }
        addProperty(result, myBorderProperty);

        final Property[] containerProperties = container.getLayoutManager().getContainerProperties(myProject);
        addApplicableProperties(containerProperties, container, result);
      }

      final RadContainer parent = component.getParent();
      if (parent != null) {
        final Property[] properties = parent.getLayoutManager().getComponentProperties(myProject, component);
        addApplicableProperties(properties, component, result);
      }

      if (component.getDelegee() instanceof AbstractButton &&
          !(component.getDelegee() instanceof JButton)) {
        addProperty(result, myButtonGroupProperty);
      }
      if (!(component instanceof RadVSpacer || component instanceof RadHSpacer)) {
        addProperty(result, ClientPropertiesProperty.getInstance(myProject));
      }

      if (component.hasIntrospectedProperties()) {
        final Class componentClass = component.getComponentClass();
        final Property[] introspectedProperties =
          Palette.getInstance(myEditor.getProject()).getIntrospectedProperties(component);
        final Properties properties = Properties.getInstance();
        for (final Property property: introspectedProperties) {
          if (!myShowExpertProperties && properties.isExpertProperty(componentClass, property.getName())) {
            continue;
          }
          addProperty(result, property);
        }
      }
    }
  }

  private void addApplicableProperties(final Property[] containerProperties,
                                       final RadComponent component,
                                       final ArrayList<Property> result) {
    for(Property prop: containerProperties) {
      if (prop.appliesTo(component)) {
        addProperty(result, prop);
      }
    }
  }

  private void addProperty(final ArrayList<Property> result, final Property property) {
    result.add(property);
    if (myExpandedProperties.contains(property.getName())) {
      for(Property child: property.getChildren(myComponent)) {
        addProperty(result, child);
      }
    }
  }

  public TableCellEditor getCellEditor(final int row, final int column){
    final PropertyEditor editor = myProperties.get(row).getEditor();
    editor.removePropertyEditorListener(myPropertyEditorListener); // we do not need to add listener on every invocation
    editor.addPropertyEditorListener(myPropertyEditorListener);
    myCellEditor.setEditor(editor);
    return myCellEditor;
  }

  public TableCellRenderer getCellRenderer(final int row,final int column){
    return myCellRenderer;
  }

  /*
   * This method is overriden due to bug in the JTree. The problem is that
   * JTree does not properly repaint edited cell if the editor is opaque or
   * has opaque child components.
   */
  public boolean editCellAt(final int row, final int column, final EventObject e){
    final boolean result = super.editCellAt(row, column, e);
    final Rectangle cellRect = getCellRect(row, column, true);
    repaint(cellRect);
    return result;
  }

  /**
   * Starts editing property with the specified <code>index</code>.
   * The method does nothing is property isn't editable.
   */
  private void startEditing(final int index){
    final Property property=myProperties.get(index);
    final PropertyEditor editor=property.getEditor();
    if(editor==null){
      return;
    }
    editCellAt(index,convertColumnIndexToView(1));
    LOG.assertTrue(editorComp!=null);
    // Now we have to request focus into the editor component
    JComponent prefComponent = editor.getPreferredFocusedComponent((JComponent)editorComp);
    if(prefComponent == null){ // use default policy to find preferred focused component
      prefComponent = IdeFocusTraversalPolicy.getPreferredFocusedComponent((JComponent)editorComp);
      LOG.assertTrue(prefComponent != null);
    }
    prefComponent.requestFocusInWindow();
  }

  private void finishEditing(){
    if(editingRow==-1){
      return;
    }
    editingStopped(new ChangeEvent(cellEditor));
  }

  public void editingStopped(final ChangeEvent ignored){
    LOG.assertTrue(isEditing());
    LOG.assertTrue(editingRow!=-1);
    final Property property=myProperties.get(editingRow);
    final PropertyEditor editor=property.getEditor();
    editor.removePropertyEditorListener(myPropertyEditorListener);
    try {
      final Object value = editor.getValue();
      setValueAt(value, editingRow, editingColumn);
    }
    catch (final Exception exc) {
      final Throwable cause = exc.getCause();
      if(cause != null){
        Messages.showMessageDialog(cause.getMessage(), UIDesignerBundle.message("title.invalid.input"), Messages.getErrorIcon());
      }
      else{
        Messages.showMessageDialog(exc.getMessage(), UIDesignerBundle.message("title.invalid.input"), Messages.getErrorIcon());
      }
    }
    finally {
      removeEditor();
    }
  }

  /**
   * Expands property with the specified index. The method fires event that
   * model changes and keeps currently selected row.
   */
  private void expandProperty(final int index){
    final int selectedRow=getSelectedRow();

    // Expand property
    final Property property=myProperties.get(index);
    LOG.assertTrue(!myExpandedProperties.contains(property));
    myExpandedProperties.add(property.getName());

    final Property[] children=property.getChildren(myComponent);
    for (int i = 0; i < children.length; i++) {
      myProperties.add(index + i + 1, children[i]);
    }
    myModel.fireTableDataChanged();

    // Restore selected row
    if(selectedRow!=-1){
      getSelectionModel().setSelectionInterval(selectedRow,selectedRow);
    }
  }

  /**
   * Collapse property with the specified index. The method fires event that
   * model changes and keeps currently selected row.
   */
  private void collapseProperty(final int index){
    final int selectedRow=getSelectedRow();

    // Expand property
    final Property property=myProperties.get(index);
    LOG.assertTrue(myExpandedProperties.contains(property.getName()));
    myExpandedProperties.remove(property.getName());

    final Property[] children=property.getChildren(myComponent);
    for (int i=0; i<children.length; i++){
      myProperties.remove(index + 1);
    }
    myModel.fireTableDataChanged();

    // Restore selected row
    if(selectedRow!=-1){
      getSelectionModel().setSelectionInterval(selectedRow,selectedRow);
    }
  }

  ErrorInfo getErrorInfoForRow(final int row){
    LOG.assertTrue(row < myProperties.size());
    final Property property = myProperties.get(row);
    ErrorInfo errorInfo = null;
    if(myClassToBindProperty.equals(property)){
      errorInfo = (ErrorInfo)myComponent.getClientProperty(ErrorAnalyzer.CLIENT_PROP_CLASS_TO_BIND_ERROR);
    }
    else if(myBindingProperty.equals(property)){
      errorInfo = (ErrorInfo)myComponent.getClientProperty(ErrorAnalyzer.CLIENT_PROP_BINDING_ERROR);
    }
    else {
      ArrayList<ErrorInfo> errors = (ArrayList<ErrorInfo>) myComponent.getClientProperty(ErrorAnalyzer.CLIENT_PROP_ERROR_ARRAY);
      if (errors != null) {
        for(ErrorInfo err: errors) {
          if (property.getName().equals(err.getPropertyName())) {
            errorInfo = err;
            break;
          }
        }
      }
    }
    return errorInfo;
  }

  /**
   * @return first error for the property at the specified row. If component doesn't contain
   * any error then the method returns <code>null</code>.
   */
  @Nullable
  private String getErrorForRow(final int row){
    LOG.assertTrue(row < myProperties.size());
    final ErrorInfo errorInfo = getErrorInfoForRow(row);
    return errorInfo != null ? errorInfo.myDescription : null;
  }

  public String getToolTipText(final MouseEvent e) {
    final int row = rowAtPoint(e.getPoint());
    if(row == -1){
      return null;
    }
    return getErrorForRow(row);
  }

  /**
   * Adapter to TableModel
   */
  private final class MyModel extends AbstractTableModel {
    private final String[] myColumnNames;

    public MyModel(){
      myColumnNames=new String[]{
        UIDesignerBundle.message("column.property"),
        UIDesignerBundle.message("column.value")};
    }

    public int getColumnCount(){
      return 2;
    }

    public String getColumnName(final int column){
      return myColumnNames[column];
    }

    public int getRowCount(){
      return myProperties.size();
    }

    public boolean isCellEditable(final int row, final int column){
      return  column==1 && myProperties.get(row).getEditor() != null;
    }

    public Object getValueAt(final int row, final int column){
      return myProperties.get(row);
    }

    public void setValueAt(final Object newValue, final int row, final int column){
      if (column != 1){
        throw new IllegalArgumentException("wrong index: " + column);
      }
      final Property property=myProperties.get(row);

      // Optimization: do nothing if value doesn't change
      final Object oldValue=property.getValue(myComponent);
      if(Comparing.equal(oldValue,newValue)){
        return;
      }
      if (!myEditor.ensureEditable()) {
        return;
      }
      try {
        property.setValue(myComponent, newValue);
      }
      catch (Throwable e) {
        if(e instanceof InvocationTargetException){ // special handling of warapped exceptions
          e = ((InvocationTargetException)e).getTargetException();
        }
        Messages.showMessageDialog(e.getMessage(), UIDesignerBundle.message("title.invalid.input"), Messages.getErrorIcon());
        return;
      }

      myEditor.refreshAndSave(false);
    }
  }

  private final class MyPropertyEditorListener extends PropertyEditorAdapter{
    public void valueCommited(final PropertyEditor source){
      if(isEditing()){
        final Object value;
        try {
          value = cellEditor.getCellEditorValue();
        }
        catch (final Exception exc) {
          final Throwable cause = exc.getCause();
          if(cause != null){
            Messages.showMessageDialog(cause.getMessage(), UIDesignerBundle.message("title.invalid.input"), Messages.getErrorIcon());
          }
          else{
            Messages.showMessageDialog(exc.getMessage(), UIDesignerBundle.message("title.invalid.input"), Messages.getErrorIcon());
          }
          return;
        }
        setValueAt(value, editingRow, editingColumn);
      }
    }

    public void editingCanceled(final PropertyEditor source) {
      if(isEditing()){
        cellEditor.cancelCellEditing();
      }
    }
  }

  private final class MyCompositeTableCellRenderer implements TableCellRenderer{
    /**
     * This renderer paints first column with property names
     */
    private final ColoredTableCellRenderer myPropertyNameRenderer;
    private final Icon myExpandIcon;
    private final Icon myCollapseIcon;
    private final Icon myLevel1ShiftIcon;
    private final Icon myLevel2ShiftIcon;

    public MyCompositeTableCellRenderer(){
      myPropertyNameRenderer = new ColoredTableCellRenderer() {
        protected void customizeCellRenderer(
          final JTable table,
          final Object value,
          final boolean selected,
          final boolean hasFocus,
          final int row,
          final int column
        ) {
          // We will append text later in the
          setPaintFocusBorder(false);
          setFocusBorderAroundIcon(true);
        }
      };
      myExpandIcon=IconLoader.getIcon("/com/intellij/uiDesigner/icons/expandNode.png");
      myCollapseIcon=IconLoader.getIcon("/com/intellij/uiDesigner/icons/collapseNode.png");
      myLevel1ShiftIcon=new EmptyIcon(9, 9);
      myLevel2ShiftIcon= new EmptyIcon(20, 9);
    }

    public Component getTableCellRendererComponent(
      final JTable table,
      final Object value,
      final boolean selected,
      final boolean hasFocus,
      final int row,
      int column
    ){
      LOG.assertTrue(value!=null);

      myPropertyNameRenderer.getTableCellRendererComponent(table,value,selected,hasFocus,row,column);

      column=table.convertColumnIndexToModel(column);
      final Property property=(Property)value;

      final Color background;
      if (property instanceof IntrospectedProperty){
        background = table.getBackground();
      }
      else {
        // syntetic property
        background = property.getParent() == null ? SYNTETIC_PROPERTY_BACKGROUND : SYNTETIC_SUBPROPERTY_BACKGROUND;
      }

      if (!selected){
        myPropertyNameRenderer.setBackground(background);
      }

      if(column==0){ // painter for first column
        SimpleTextAttributes attrs = getTextAttributes(row, property);
        myPropertyNameRenderer.append(property.getName(), attrs);

        // 2. Icon
        if(property.getChildren(myComponent).length>0){
          // This is composite property and we have to show +/- sign
          if(myExpandedProperties.contains(property.getName())){
            myPropertyNameRenderer.setIcon(myCollapseIcon);
          }else{
            myPropertyNameRenderer.setIcon(myExpandIcon);
          }
        }else{
          // If property doesn't have children then we have shift its text
          // to the right
          if(property.getParent()!=null){
            // Second level has larger shift
            myPropertyNameRenderer.setIcon(myLevel2ShiftIcon);
          }else{
            myPropertyNameRenderer.setIcon(myLevel1ShiftIcon);
          }
        }
      }
      else if(column==1){ // painter for second column
        final PropertyRenderer renderer=property.getRenderer();
        final JComponent component = renderer.getComponent(myComponent, property.getValue(myComponent),selected,hasFocus);
        if (!selected) {
          component.setBackground(background);
        }
        if (property.isModified(myComponent)) {
          component.setFont(table.getFont().deriveFont(Font.BOLD));
        }
        else {
          component.setFont(table.getFont());
        }
        return component;
      }
      else{
        throw new IllegalArgumentException("wrong column: "+column);
      }

      if (!selected) {
        myPropertyNameRenderer.setForeground(PropertyInspectorTable.this.getForeground());
        if(property instanceof IntrospectedProperty){
          final Class componentClass = myComponent.getComponentClass();
          if (Properties.getInstance().isExpertProperty(componentClass, property.getName())) {
            myPropertyNameRenderer.setForeground(Color.LIGHT_GRAY);
          }
        }
      }

      return myPropertyNameRenderer;
    }

    private SimpleTextAttributes getTextAttributes(final int row, final Property property) {
      // 1. Text
      ErrorInfo errInfo = getErrorInfoForRow(row);

      if (errInfo == null) {
        return property.isModified(myComponent) ? SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES : SimpleTextAttributes.REGULAR_ATTRIBUTES;
      }

      boolean modified = property.isModified(myComponent);
      final HighlightSeverity severity = errInfo.getHighlightDisplayLevel().getSeverity();
      Map<HighlightSeverity, SimpleTextAttributes> cache = modified ? myModifiedHighlightAttributes : myHighlightAttributes;
      SimpleTextAttributes result = cache.get(severity);
      if (result == null) {
        final TextAttributesKey attrKey = SeverityRegistrar.getHighlightInfoTypeBySeverity(severity).getAttributesKey();
        TextAttributes textAttrs = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(attrKey);
        if (modified) {
          textAttrs = textAttrs.clone();
          textAttrs.setFontType(textAttrs.getFontType() | Font.BOLD);
        }
        result = SimpleTextAttributes.fromTextAttributes(textAttrs);
        cache.put(severity, result);
      }

      return result;
    }
  }

  /**
   * This is adapter from PropertyEditor to TableCellEditor interface
   */
  private final class MyCellEditor extends AbstractCellEditor implements TableCellEditor{
    private PropertyEditor myEditor;

    public void setEditor(final PropertyEditor editor){
      if (editor == null) {
        throw new IllegalArgumentException("editor cannot be null");
      }
      myEditor = editor;
    }

    public Object getCellEditorValue(){
      try {
        return myEditor.getValue();
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column){
      LOG.assertTrue(value!=null);
      final Property property=(Property)value;
      return myEditor.getComponent(myComponent, property.getValue(myComponent), false);
    }
  }

  /**
   * Expands/collapses rows
   */
  private final class MyMouseListener extends MouseAdapter {
    public void mousePressed(final MouseEvent e){
      final int row = rowAtPoint(e.getPoint());
      if (row == -1){
        return;
      }
      final Rectangle rect = getCellRect(row, convertColumnIndexToView(0), false);
      if (e.getX() < rect.x || e.getX() > rect.x + 9 || e.getY() < rect.y || e.getY() > rect.y + rect.height) {
        return;
      }

      final Property property = myProperties.get(row);
      final Property[] children = property.getChildren(myComponent);
      if (children.length == 0) {
        return;
      }

      if (myExpandedProperties.contains(property.getName())) {
        collapseProperty(row);
      }
      else {
        expandProperty(row);
      }
    }

    public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
        int row = rowAtPoint(e.getPoint());
        int column = columnAtPoint(e.getPoint());
        if (row >= 0 && column == 0) {
          final Property property = myProperties.get(row);
          if (property.getChildren(myComponent).length == 0) {
            startEditing(row);
          }
        }
      }
    }
  }

  /**
   * Reimplementation of LookAndFeel's SelectPreviousRowAction action.
   * Standard implementation isn't smart enough.
   *
   * @see javax.swing.plaf.basic.BasicTableUI
   */
  private final class MySelectPreviousRowAction extends AbstractAction{
    public void actionPerformed(final ActionEvent e){
      final int rowCount=getRowCount();
      LOG.assertTrue(rowCount>0);
      int selectedRow=getSelectedRow();
      if(selectedRow!=-1){
        selectedRow -= 1;
      }
      selectedRow=(selectedRow+rowCount)%rowCount;
      if(isEditing()){
        finishEditing();
        getSelectionModel().setSelectionInterval(selectedRow,selectedRow);
        startEditing(selectedRow);
      } else {
        getSelectionModel().setSelectionInterval(selectedRow,selectedRow);
      }
    }
  }

  /**
   * Reimplementation of LookAndFeel's SelectNextRowAction action.
   * Standard implementation isn't smart enough.
   *
   * @see javax.swing.plaf.basic.BasicTableUI
   */
  private final class MySelectNextRowAction extends AbstractAction{
    public void actionPerformed(final ActionEvent e){
      final int rowCount=getRowCount();
      LOG.assertTrue(rowCount>0);
      final int selectedRow=(getSelectedRow()+1)%rowCount;
      if(isEditing()){
        finishEditing();
        getSelectionModel().setSelectionInterval(selectedRow,selectedRow);
        startEditing(selectedRow);
      }else{
        getSelectionModel().setSelectionInterval(selectedRow,selectedRow);
      }
    }
  }

  /**
   * Reimplementation of LookAndFeel's StartEditingAction action.
   * Standard implementation isn't smart enough.
   *
   * @see javax.swing.plaf.basic.BasicTableUI
   */
  private final class MyStartEditingAction extends AbstractAction{
    public void actionPerformed(final ActionEvent e){
      final int selectedRow=getSelectedRow();
      if(selectedRow==-1 || isEditing()){
        return;
      }

      startEditing(selectedRow);
    }
  }

  /**
   * Expands property which has children or start editing atomic
   * property.
   */
  private final class MyEnterAction extends AbstractAction{
    public void actionPerformed(final ActionEvent e){
      final int selectedRow=getSelectedRow();
      if(isEditing() || selectedRow==-1){
        return;
      }

      final Property property=myProperties.get(selectedRow);
      if(property.getChildren(myComponent).length>0){
        if(myExpandedProperties.contains(property.getName())){
          collapseProperty(selectedRow);
        }else{
          expandProperty(selectedRow);
        }
      }else{
        startEditing(selectedRow);
      }
    }
  }

  private class MyExpandCurrentAction extends AbstractAction {
    private boolean myExpand;

    public MyExpandCurrentAction(final boolean expand) {
      myExpand = expand;
    }

    public void actionPerformed(ActionEvent e) {
      final int selectedRow=getSelectedRow();
      if(isEditing() || selectedRow==-1){
        return;
      }
      final Property property=myProperties.get(selectedRow);
      if(property.getChildren(myComponent).length>0) {
        if (myExpand) {
          if (!myExpandedProperties.contains(property.getName())) {
            expandProperty(selectedRow);
          }
        }
        else {
          if (myExpandedProperties.contains(property.getName())) {
            collapseProperty(selectedRow);
          }
        }
      }
    }
  }

  /**
   * Updates UI of editors and renderers of all introspected properties
   */
  private final class MyLafManagerListener implements LafManagerListener{
    /**
     * Recursively updates renderer and editor UIs of all synthetic
     * properties.
     */
    private void updateUI(final Property property){
      final PropertyRenderer renderer = property.getRenderer();
      renderer.updateUI();
      final PropertyEditor editor = property.getEditor();
      if(editor != null){
        editor.updateUI();
      }
      final Property[] children = property.getChildren(myComponent);
      for (int i = children.length - 1; i >= 0; i--) {
        final Property child = children[i];
        if(!(child instanceof IntrospectedProperty)){
          updateUI(child);
        }
      }
    }

    public void lookAndFeelChanged(final LafManager source) {
      updateUI(myBorderProperty);
      updateUI(MarginProperty.getInstance(myProject));
      updateUI(HGapProperty.getInstance(myProject));
      updateUI(VGapProperty.getInstance(myProject));
      updateUI(HSizePolicyProperty.getInstance(myProject));
      updateUI(VSizePolicyProperty.getInstance(myProject));
      updateUI(FillProperty.getInstance(myProject));
      updateUI(AnchorProperty.getInstance(myProject));
      updateUI(RowSpanProperty.getInstance(myProject));
      updateUI(ColumnSpanProperty.getInstance(myProject));
      updateUI(IndentProperty.getInstance(myProject));
      updateUI(UseParentLayoutProperty.getInstance(myProject));
      updateUI(MinimumSizeProperty.getInstance(myProject));
      updateUI(PreferredSizeProperty.getInstance(myProject));
      updateUI(MaximumSizeProperty.getInstance(myProject));
      updateUI(myButtonGroupProperty);
      updateUI(SameSizeHorizontallyProperty.getInstance(myProject));
      updateUI(SameSizeVerticallyProperty.getInstance(myProject));
      updateUI(CustomCreateProperty.getInstance(myProject));
      updateUI(ClientPropertiesProperty.getInstance(myProject));
    }
  }


}