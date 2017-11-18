/*
 * Copyright 2006 Sascha Weinreuter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.plugins.intelliLang.inject.config.ui;

import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.actionSystem.TypeSafeDataProvider;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.actionSystem.DataSink;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.peer.PeerFactory;
import com.intellij.psi.*;
import com.intellij.psi.impl.compiled.ClsParameterListImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiFormatUtil;
import com.intellij.ui.BooleanTableCellRenderer;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.ReferenceEditorWithBrowseButton;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.dualView.TreeTableView;
import com.intellij.util.Function;
import com.intellij.util.Icons;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.tree.TreeUtil;
import com.intellij.util.ui.treetable.ListTreeTableModelOnColumns;
import com.intellij.util.ui.treetable.TreeColumnInfo;
import gnu.trove.THashMap;
import org.intellij.plugins.intelliLang.inject.config.MethodParameterInjection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class MethodParameterPanel extends AbstractInjectionPanel<MethodParameterInjection> {

  LanguagePanel myLanguagePanel;  // read by reflection

  private JPanel myRoot;
  private JPanel myClassPanel;
  private JCheckBox myHierarchy;

  private TreeTableView myParamsTable;

  private ReferenceEditorWithBrowseButton myClassField;
  private DefaultMutableTreeNode myRootNode;

  private THashMap<PsiMethod, MethodParameterInjection.MethodInfo> myData = new THashMap<PsiMethod, MethodParameterInjection.MethodInfo>();

  public MethodParameterPanel(MethodParameterInjection injection, final Project project) {
    super(injection, project);

    myClassField = new ReferenceEditorWithBrowseButton(new BrowseClassListener(project), project, new Function<String, Document>() {
      public Document fun(String s) {
        final Document document = ReferenceEditorWithBrowseButton.createTypeDocument(s, PsiManager.getInstance(project));
        document.addDocumentListener(new DocumentAdapter() {
          @Override
          public void documentChanged(final DocumentEvent e) {
            updateParamTree();
            updateTree();
          }
        });
        return document;
      }
    }, "");
    myClassPanel.add(myClassField, BorderLayout.CENTER);
    myParamsTable.getTree().setShowsRootHandles(true);
    myParamsTable.getTree().setCellRenderer(new ColoredTreeCellRenderer() {

      public void customizeCellRenderer(final JTree tree,
                                        final Object value,
                                        final boolean selected,
                                        final boolean expanded,
                                        final boolean leaf,
                                        final int row,
                                        final boolean hasFocus) {

        final Object o = ((DefaultMutableTreeNode)value).getUserObject();
        setIcon(o instanceof PsiMethod ? Icons.METHOD_ICON : o instanceof PsiParameter ? Icons.PARAMETER_ICON : null);
        final String name = o instanceof PsiMethod
                            ? PsiFormatUtil.formatMethod((PsiMethod)o, PsiSubstitutor.EMPTY,
                                                         PsiFormatUtil.SHOW_NAME | PsiFormatUtil.SHOW_PARAMETERS, PsiFormatUtil.SHOW_NAME)
                            : o instanceof PsiParameter
                              ? PsiFormatUtil
                                .formatVariable((PsiParameter)o, PsiFormatUtil.SHOW_NAME | PsiFormatUtil.SHOW_TYPE, PsiSubstitutor.EMPTY)
                              : null;
        final boolean missing = o instanceof PsiElement && !((PsiElement)o).isPhysical();
        if (name != null) {
          append(name, missing? SimpleTextAttributes.ERROR_ATTRIBUTES : SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
      }

    });
    init(injection.copy());
    PeerFactory.getInstance().getUIHelper().installTreeTableSpeedSearch(myParamsTable, new Convertor<TreePath, String>() {
      @Nullable
      public String convert(final TreePath o) {
        final Object userObject = ((DefaultMutableTreeNode)o.getLastPathComponent()).getUserObject();
        return userObject instanceof PsiNamedElement? ((PsiNamedElement)userObject).getName() : null;
      }
    });
  }

  @Nullable
  private PsiType getClassType() {
    final Document document = myClassField.getEditorTextField().getDocument();
    final PsiDocumentManager dm = PsiDocumentManager.getInstance(myProject);
    dm.commitDocument(document);
    final PsiFile psiFile = dm.getPsiFile(document);
    try {
      assert psiFile != null;
      return ((PsiTypeCodeFragment)psiFile).getType();
    }
    catch (PsiTypeCodeFragment.TypeSyntaxException e1) {
      return null;
    }
    catch (PsiTypeCodeFragment.NoTypeException e1) {
      return null;
    }
  }

  @Nullable
  private PsiClass findClass(PsiType type) {
    if (type instanceof PsiClassType) {
      final JavaPsiFacade facade = JavaPsiFacade.getInstance(myProject);
      return facade.findClass(type.getCanonicalText(), GlobalSearchScope.allScope(myProject));
    }
    return null;
  }

  private void setPsiClass(String name) {
    myClassField.setText(name);
  }

  private void updateParamTree() {
    rebuildTreeModel();
    refreshTreeStructure();
  }

  private void rebuildTreeModel() {
    myData.clear();
    final PsiClass psiClass = findClass(getClassType());
    if (psiClass == null) return;
    final List<PsiMethod> methods = ContainerUtil.findAll(psiClass.getMethods(), new Condition<PsiMethod>() {
      public boolean value(final PsiMethod method) {
        final PsiModifierList modifiers = method.getModifierList();
        if (modifiers.hasModifierProperty(PsiModifier.PRIVATE) || modifiers.hasModifierProperty(PsiModifier.PACKAGE_LOCAL)) {
          return false;
        }
        final PsiParameter[] parameters = method.getParameterList().getParameters();
        return null != ContainerUtil.find(parameters, new Condition<PsiParameter>() {
          public boolean value(PsiParameter p) {
            return isInjectable(p.getType());
          }
        });
      }
    });
    for (PsiMethod method : methods) {
      final String signature = buildSignature(method);
      myData.put(method, new MethodParameterInjection.MethodInfo(signature, new boolean[method.getParameterList().getParametersCount()]));
    }
  }

  private void refreshTreeStructure() {
    myRootNode.removeAllChildren();
    final ArrayList<PsiMethod> methods = new ArrayList<PsiMethod>(myData.keySet());
    Collections.sort(methods, new Comparator<PsiMethod>() {
      public int compare(final PsiMethod o1, final PsiMethod o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    for (PsiMethod method : methods) {
      final PsiParameter[] params = method.getParameterList().getParameters();
      final DefaultMutableTreeNode methodNode = new DefaultMutableTreeNode(method, true);
      myRootNode.add(methodNode);
      for (final PsiParameter parameter : params) {
        methodNode.add(new DefaultMutableTreeNode(parameter, false));
      }
    }
    final ListTreeTableModelOnColumns tableModel = (ListTreeTableModelOnColumns)myParamsTable.getTableModel();
    tableModel.reload();
    TreeUtil.expandAll(myParamsTable.getTree());
    myParamsTable.revalidate();
  }

  public String getClassName() {
    final PsiType type = getClassType();
    if (type == null) {
      return myClassField.getText();
    }
    return type.getCanonicalText();
  }


  protected void apply(MethodParameterInjection other) {
    other.setClassName(getClassName());
    other.setApplyInHierarchy(myHierarchy.isSelected());
    if (getClassType() != null) {
      other.setMethodInfos(ContainerUtil.findAll(myData.values(), new Condition<MethodParameterInjection.MethodInfo>() {
        public boolean value(final MethodParameterInjection.MethodInfo methodInfo) {
          return methodInfo.isEnabled();
        }
      }));
    }
  }

  protected void resetImpl() {
    setPsiClass(myOrigInjection.getClassName());
    myHierarchy.setSelected(myOrigInjection.isApplyInHierarchy());

    rebuildTreeModel();
    final THashMap<String, PsiMethod> map = new THashMap<String, PsiMethod>();
    for (PsiMethod method : myData.keySet()) {
      map.put(myData.get(method).getMethodSignature(), method);
    }
    for (MethodParameterInjection.MethodInfo info : myOrigInjection.getMethodInfos()) {
      final PsiMethod method = map.get(info.getMethodSignature());
      if (method != null) {
        final MethodParameterInjection.MethodInfo curInfo = myData.get(method);
        System.arraycopy(info.getParamFlags(), 0, curInfo.getParamFlags(), 0, Math.min(info.getParamFlags().length, curInfo.getParamFlags().length));
      }
      else {
        final PsiMethod missingMethod = makeMethod(info.getMethodSignature());
        myData.put(missingMethod, info.copy());
      }
    }
    refreshTreeStructure();
    final Enumeration enumeration = myRootNode.children();
    while (enumeration.hasMoreElements()) {
      PsiMethod method = (PsiMethod)((DefaultMutableTreeNode)enumeration.nextElement()).getUserObject();
      assert myData.containsKey(method);
    }
  }

  @NotNull
  private static String buildSignature(@NotNull PsiMethod method) {
    final PsiParameterList list = method.getParameterList();
    final PsiParameter[] parameters = list.getParameters();
    final String s;
    if (parameters.length > 0) {
      // if there are no sources, parameter names are unknown. This trick gives the "decompiled" names
      if (list instanceof ClsParameterListImpl && parameters[0].getName() == null) {
        s = method.getName() + list.getText();
      }
      else {
        s = PsiFormatUtil.formatMethod(method, PsiSubstitutor.EMPTY, PsiFormatUtil.SHOW_NAME | PsiFormatUtil.SHOW_PARAMETERS,
                                       PsiFormatUtil.SHOW_TYPE | PsiFormatUtil.SHOW_NAME | PsiFormatUtil.SHOW_FQ_CLASS_NAMES);
      }
    }
    else {
      s = PsiFormatUtil.formatMethod(method, PsiSubstitutor.EMPTY, PsiFormatUtil.SHOW_NAME, 0) + "()";
    }
    return s;
  }

  @Nullable
  private PsiMethod makeMethod(String signature) {
    try {
      if (signature.trim().length() > 0) {
        final JavaPsiFacade facade = JavaPsiFacade.getInstance(myProject);
        final PsiElementFactory ef = facade.getElementFactory();
        return ef.createMethodFromText("void " + signature + "{}", null);
      }
    }
    catch (IncorrectOperationException e) {
      // signature is not in form NAME(TYPE NAME)
    }
    return null;
  }

  public JPanel getComponent() {
    return myRoot;
  }

  private void createUIComponents() {
    myLanguagePanel = new LanguagePanel(myProject, myOrigInjection);
    myRootNode = new DefaultMutableTreeNode(null, true);
    myParamsTable = new MyView(new ListTreeTableModelOnColumns(myRootNode, createColumnInfos()));
  }

  private ColumnInfo[] createColumnInfos() {
    return new ColumnInfo[]{
        new ColumnInfo<DefaultMutableTreeNode, Boolean>(" ") { // "" for the first column's name isn't a good idea
          final BooleanTableCellRenderer myRenderer = new BooleanTableCellRenderer();

          public Boolean valueOf(DefaultMutableTreeNode o) {
            final Object userObject = o.getUserObject();
            if (userObject instanceof PsiParameter) {
              final PsiMethod method = getMethod(o);
              final int index = method.getParameterList().getParameterIndex((PsiParameter)userObject);
              return myData.get(method).getParamFlags()[index];
            }
            return null;
          }

          public int getWidth(JTable table) {
            return myRenderer.getPreferredSize().width;
          }

          public TableCellEditor getEditor(DefaultMutableTreeNode o) {
            return new DefaultCellEditor(new JCheckBox());
          }

          public TableCellRenderer getRenderer(DefaultMutableTreeNode o) {
            myRenderer.setEnabled(isCellEditable(o));
            return myRenderer;
          }

          public void setValue(DefaultMutableTreeNode o, Boolean value) {
            final Object userObject = o.getUserObject();
            if (userObject instanceof PsiParameter) {
              final PsiMethod method = getMethod(o);
              final int index = method.getParameterList().getParameterIndex((PsiParameter)userObject);
              myData.get(method).getParamFlags()[index] = Boolean.TRUE.equals(value);
            }
          }

          public Class<Boolean> getColumnClass() {
            return Boolean.class;
          }

          public boolean isCellEditable(DefaultMutableTreeNode o) {
            return o.getUserObject() instanceof PsiParameter && isInjectable(((PsiParameter)o.getUserObject()).getType());
          }

          private PsiMethod getMethod(final DefaultMutableTreeNode o) {
            return (PsiMethod)((DefaultMutableTreeNode)o.getParent()).getUserObject();
          }


        }, new TreeColumnInfo("Method/Parameters")
    };
  }

  public static boolean isInjectable(PsiType type) {
    return type.equalsToText("java.lang.String") || type.equalsToText("java.lang.String...") || type.equalsToText("java.lang.String[]");
  }

  private class BrowseClassListener implements ActionListener {
    private final Project myProject;

    public BrowseClassListener(Project project) {
      myProject = project;
    }

    public void actionPerformed(ActionEvent e) {
      final TreeClassChooserFactory factory = TreeClassChooserFactory.getInstance(myProject);
      final TreeClassChooser chooser = factory.createAllProjectScopeChooser("Select Class");
      chooser.showDialog();
      final PsiClass psiClass = chooser.getSelectedClass();
      if (psiClass != null) {
        setPsiClass(psiClass.getQualifiedName());
        updateParamTree();
        updateTree();
      }
    }
  }

  private class MyView extends TreeTableView implements TypeSafeDataProvider {
    public MyView(ListTreeTableModelOnColumns treeTableModel) {
      super(treeTableModel);
    }

    public void calcData(final DataKey key, final DataSink sink) {
      if (LangDataKeys.PSI_ELEMENT.equals(key)) {
        final Collection selection = getSelection();
        if (!selection.isEmpty()) {
          final Object o = ((DefaultMutableTreeNode)selection.iterator().next()).getUserObject();
          if (o instanceof PsiElement) sink.put(LangDataKeys.PSI_ELEMENT, (PsiElement)o);
        }
      }
    }
  }
}