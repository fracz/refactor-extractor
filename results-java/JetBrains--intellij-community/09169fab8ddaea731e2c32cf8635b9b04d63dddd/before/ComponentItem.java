package com.intellij.uiDesigner.palette;

import com.intellij.ide.dnd.DnDDragStartBean;
import com.intellij.ide.palette.PaletteItem;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.impl.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.lw.StringDescriptor;
import com.intellij.uiDesigner.propertyInspector.IntrospectedProperty;
import com.intellij.uiDesigner.designSurface.ComponentDragObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Anton Katilin
 * @author Vladimir Kondratyev
 */
public final class ComponentItem implements Cloneable, PaletteItem, ComponentDragObject {
  private static final Logger LOG = Logger.getInstance("#com.intellij.uiDesigner.palette.ComponentItem");

  private String myClassName;
  private final GridConstraints myDefaultConstraints;
  /**
   * Do not use this member directly. Use {@link #getIcon()} instead.
   */
  private Icon myIcon;
  /**
   * Do not use this member directly. Use {@link #getSmallIcon()} instead.
   */
  private Icon mySmallIcon;
  /**
   * @see #getIconPath()
   * @see #setIconPath(java.lang.String)
   */
  private String myIconPath;
  /**
   * Do not access this field directly. Use {@link #getToolTipText()} instead.
   */
  final String myToolTipText;
  private final HashMap<String, StringDescriptor> myPropertyName2intitialValue;
  /** Whether item is removable or not */
  private final boolean myRemovable;

  private boolean myAutoCreateBinding;
  private boolean myCanAttachLabel;

  @NotNull private Project myProject;

  public ComponentItem(
    @NotNull Project project,
    final String className,
    @Nullable final String iconPath,
    @Nullable final String toolTipText,
    @NotNull final GridConstraints defaultConstraints,
    @NotNull final HashMap<String, StringDescriptor> propertyName2initialValue,
    final boolean removable,
    final boolean autoCreateBinding,
    final boolean canAttachLabel
  ){
    myAutoCreateBinding = autoCreateBinding;
    myCanAttachLabel = canAttachLabel;
    myProject = project;
    setClassName(className);
    setIconPath(iconPath);

    myToolTipText = toolTipText;
    myDefaultConstraints = defaultConstraints;
    myPropertyName2intitialValue = propertyName2initialValue;

    myRemovable = removable;
  }

  /**
   * @return whether the item is removable from palette or not.
   */
  public boolean isRemovable() {
    return myRemovable;
  }

  private static String calcToolTipText(@NotNull final String className) {
    final int lastDotIndex = className.lastIndexOf('.');
    if (lastDotIndex != -1 && lastDotIndex != className.length() - 1/*not the last char in class name*/) {
      return className.substring(lastDotIndex + 1) + " (" + className.substring(0, lastDotIndex) + ")";
    }
    else{
      return className;
    }
  }

  /** Creates deep copy of the object. You can edit any properties of the returned object. */
  public ComponentItem clone(){
    final ComponentItem result = new ComponentItem(
      myProject,
      myClassName,
      myIconPath,
      myToolTipText,
      (GridConstraints)myDefaultConstraints.clone(),
      (HashMap<String, StringDescriptor>)myPropertyName2intitialValue.clone(),
      myRemovable,
      myAutoCreateBinding,
      myCanAttachLabel
    );
    return result;
  }

  /**
   * @return string that represents path in the JAR file system that was used to load
   * icon returned by {@link #getIcon()} method. This method can returns <code>null</code>.
   * It means that palette item has some "unknown" item.
   */
  @Nullable String getIconPath() {
    return myIconPath;
  }

  /**
   * @param iconPath new path inside JAR file system. <code>null</code> means that
   * <code>iconPath</code> is not specified and some "unknown" icon should be used
   * to represent the {@link ComponentItem} in UI.
   */
  void setIconPath(@Nullable final String iconPath){
    myIcon = null; // reset cached icon
    mySmallIcon = null; // reset cached icon

    myIconPath = iconPath;
  }

  /**
   * @return item's icon. This icon is used to represent item at the toolbar.
   * Note, that the method never returns <code>null</code>. It returns some
   * default "unknown" icon for the items that has no specified icon in the XML.
   */
  @NotNull public Icon getIcon() {
    // Check cached value first
    if(myIcon != null){
      return myIcon;
    }

    // Create new icon
    if(myIconPath != null) {
      final VirtualFile iconFile = ModuleUtil.findResourceFileInProject(myProject, myIconPath);
      if (iconFile != null) {
        try {
          myIcon = new ImageIcon(iconFile.contentsToByteArray());
        }
        catch (IOException e) {
          myIcon = null;
        }
      }
      else {
        myIcon = IconLoader.findIcon(myIconPath);
      }
    }
    if(myIcon == null){
      myIcon = IconLoader.getIcon("/com/intellij/uiDesigner/icons/unknown.png");
    }
    LOG.assertTrue(myIcon != null);
    return myIcon;
  }

  /**
   * @return small item's icon. This icon represents component in the
   * component tree. The method never returns <code>null</code>. It returns some
   * default "unknown" icon for the items that has no specified icon in the XML.
   */
  @NotNull public Icon getSmallIcon() {
    // Check cached value first
    if(mySmallIcon != null){
      return myIcon;
    }

    // [vova] It's safe to cast to ImageIcon here because all icons loaded by IconLoader
    // are ImageIcon(s).
    final ImageIcon icon = (ImageIcon)getIcon();
    mySmallIcon = new MySmallIcon(icon.getImage());
    return mySmallIcon;
  }

  /**
   * @return name of component's class which is represented by the item.
   */
  @NotNull public String getClassName() {
    return myClassName;
  }

  private String getClassShortName() {
    final int lastDotIndex = myClassName.lastIndexOf('.');
    if (lastDotIndex != -1 && lastDotIndex != myClassName.length() - 1/*not the last char in class name*/) {
      return myClassName.substring(lastDotIndex + 1).replace('$', '.');
    }
    else{
      return myClassName.replace('$', '.');
    }
  }

  /**
   * @param className name of the class that will be instanteated when user drop
   * item on the form. Cannot be <code>null</code>. If the class does not exist or
   * could not be instanteated (for example, class has no default constructor,
   * it's not a subclass of JComponent, etc) then placeholder component will be
   * added to the form.
   */
  public void setClassName(@NotNull final String className){
    myClassName = className;
  }

  public String getToolTipText() {
    return myToolTipText != null ? myToolTipText : calcToolTipText(myClassName);
  }

  @NotNull public GridConstraints getDefaultConstraints() {
    return myDefaultConstraints;
  }

  /**
   * The method returns initial value of the property. Term
   * "initial" means that just after creation of RadComponent
   * all its properties are set into initial values.
   * The method returns <code>null</code> if the
   * initial property is not defined. Unfortunately we cannot
   * put this method into the constuctor of <code>RadComponent</code>.
   * The problem is that <code>RadComponent</code> is used in the
   * code genaration and code generation doesn't depend on any
   * <code>ComponentItem</code>, so we need to initialize <code>RadComponent</code>
   * in all places where it's needed explicitly.
   */
  public Object getInitialValue(final IntrospectedProperty property){
    return myPropertyName2intitialValue.get(property.getName());
  }

  /**
   * Internal method. It should be used only to externalize initial item's values.
   * This method never returns <code>null</code>.
   */
  HashMap<String, StringDescriptor> getInitialValues(){
    return myPropertyName2intitialValue;
  }

  public boolean isAutoCreateBinding() {
    return myAutoCreateBinding;
  }

  public void setAutoCreateBinding(final boolean autoCreateBinding) {
    myAutoCreateBinding = autoCreateBinding;
  }

  public boolean isCanAttachLabel() {
    return myCanAttachLabel;
  }

  public void setCanAttachLabel(final boolean canAttachLabel) {
    myCanAttachLabel = canAttachLabel;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof ComponentItem)) return false;

    final ComponentItem componentItem = (ComponentItem)o;

    if (myClassName != null ? !myClassName.equals(componentItem.myClassName) : componentItem.myClassName != null) return false;
    if (myDefaultConstraints != null
        ? !myDefaultConstraints.equals(componentItem.myDefaultConstraints)
        : componentItem.myDefaultConstraints != null) {
      return false;
    }
    if (myIconPath != null ? !myIconPath.equals(componentItem.myIconPath) : componentItem.myIconPath != null) return false;
    if (myPropertyName2intitialValue != null
        ? !myPropertyName2intitialValue.equals(componentItem.myPropertyName2intitialValue)
        : componentItem.myPropertyName2intitialValue != null) {
      return false;
    }
    if (myToolTipText != null ? !myToolTipText.equals(componentItem.myToolTipText) : componentItem.myToolTipText != null) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (myClassName != null ? myClassName.hashCode() : 0);
    result = 29 * result + (myDefaultConstraints != null ? myDefaultConstraints.hashCode() : 0);
    result = 29 * result + (myIconPath != null ? myIconPath.hashCode() : 0);
    result = 29 * result + (myToolTipText != null ? myToolTipText.hashCode() : 0);
    result = 29 * result + (myPropertyName2intitialValue != null ? myPropertyName2intitialValue.hashCode() : 0);
    return result;
  }

  public void customizeCellRenderer(ColoredListCellRenderer cellRenderer, boolean selected, boolean hasFocus) {
    cellRenderer.setIcon(getSmallIcon());
    cellRenderer.append(getClassShortName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    cellRenderer.setToolTipText(getToolTipText());
  }

  @Nullable public DnDDragStartBean startDragging() {
    return new DnDDragStartBean(this);
  }

  @Nullable public ActionGroup getPopupActionGroup() {
    return (ActionGroup) ActionManager.getInstance().getAction("GuiDesigner.PaletteComponentPopupMenu");
  }

  @Nullable public Object getData(Project project, String dataId) {
    if (dataId.equals(DataConstants.PSI_ELEMENT)) {
      return PsiManager.getInstance(project).findClass(
        myClassName,
        GlobalSearchScope.allScope(project));
    }
    if (dataId.equals(getClass().getName())) {
      return this;
    }
    return null;
  }

  @Nullable public PsiFile getBoundForm() {
    PsiManager psiManager = PsiManager.getInstance(myProject);
    PsiFile[] boundForms = psiManager.getSearchHelper().findFormsBoundToClass(myClassName.replace('$', '.'));
    if (boundForms != null && boundForms.length > 0) {
      return boundForms [0];
    }
    return null;
  }

  public int getComponentCount() {
    return 1;
  }

  public int getDragRelativeColumn() {
    return 0;
  }

  private static final class MySmallIcon implements Icon{
    private final Image myImage;

    public MySmallIcon(final Image delegate) {
      LOG.assertTrue(delegate != null);
      myImage = delegate;
    }

    public int getIconHeight() {
      return 18;
    }

    public int getIconWidth() {
      return 18;
    }

    public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
      g.drawImage(myImage, 2, 2, 14, 14, c);
    }
  }
}