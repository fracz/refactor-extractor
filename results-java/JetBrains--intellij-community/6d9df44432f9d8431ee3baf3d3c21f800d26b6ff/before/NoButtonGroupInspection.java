package com.intellij.uiDesigner.inspections;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.uiDesigner.FormEditingUtil;
import com.intellij.uiDesigner.radComponents.RadComponent;
import com.intellij.uiDesigner.radComponents.RadContainer;
import com.intellij.uiDesigner.UIDesignerBundle;
import com.intellij.uiDesigner.actions.GroupButtonsAction;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.designSurface.GuiEditor;
import com.intellij.uiDesigner.lw.IComponent;
import com.intellij.uiDesigner.lw.IContainer;
import com.intellij.uiDesigner.lw.IRootContainer;
import com.intellij.uiDesigner.quickFixes.QuickFix;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author yole
 */
public class NoButtonGroupInspection extends BaseFormInspection {
  private static final Logger LOG = Logger.getInstance("#com.intellij.uiDesigner.inspections.NoButtonGroupInspection");

  public NoButtonGroupInspection() {
    super("NoButtonGroup");
  }

  @Override public String getDisplayName() {
    return UIDesignerBundle.message("inspection.no.button.group");
  }

  protected void checkComponentProperties(Module module, IComponent component, FormErrorCollector collector) {
    if (FormInspectionUtil.isComponentClass(module, component, JRadioButton.class)) {
      final IRootContainer root = FormEditingUtil.getRoot(component);
      if (root == null) return;
      if (root.getButtonGroupName(component) == null) {
        IContainer parent = component.getParentContainer();
        for(int i=0; i<parent.getComponentCount(); i++) {
          IComponent child = parent.getComponent(i);
          if (child != component &&
              FormInspectionUtil.isComponentClass(module, child, JRadioButton.class) &&
              root.getButtonGroupName(child) == null) {
            final GridConstraints c1 = component.getConstraints();
            final GridConstraints c2 = child.getConstraints();
            if ((c1.getRow() == c2.getRow() && Math.abs(c1.getColumn() - c2.getColumn()) == 1) ||
                (c1.getColumn() == c2.getColumn() && Math.abs(c1.getRow() - c2.getRow()) == 1)) {
              collector.addError(null, UIDesignerBundle.message("inspection.no.button.group.error"),
                                 new EditorQuickFixProvider() {
                                   public QuickFix createQuickFix(GuiEditor editor, RadComponent component) {
                                     return new MyQuickFix(editor, component, c1.getColumn() == c2.getColumn());
                                   }
                                 });
            }
          }
        }
      }
    }
  }

  private static class MyQuickFix extends QuickFix {
    private RadComponent myComponent;
    private boolean myVerticalGroup;

    public MyQuickFix(final GuiEditor editor, final RadComponent component, boolean verticalGroup) {
      super(editor, UIDesignerBundle.message("inspection.no.button.group.quickfix"));
      myVerticalGroup = verticalGroup;
      myComponent = component;
    }

    public void run() {
      RadContainer parent = myComponent.getParent();
      ArrayList<RadComponent> buttonsToGroup = new ArrayList<RadComponent>();
      for(RadComponent component: parent.getComponents()) {
        if (FormInspectionUtil.isComponentClass(myComponent.getModule(), component, JRadioButton.class)) {
          if (component.getConstraints().getCell(!myVerticalGroup) == myComponent.getConstraints().getCell(!myVerticalGroup))
            buttonsToGroup.add(component);
        }
      }
      Collections.sort(buttonsToGroup, new Comparator<RadComponent>() {
        public int compare(final RadComponent o1, final RadComponent o2) {
          if (myVerticalGroup) {
            return o1.getConstraints().getRow() - o2.getConstraints().getRow();
          }
          return o1.getConstraints().getColumn() - o2.getConstraints().getColumn();
        }
      });

      // ensure that selected radio buttons are in adjacent cells, and exclude from grouping
      // buttons separated by empty cells or other controls
      int index=buttonsToGroup.indexOf(myComponent);
      LOG.assertTrue(index >= 0);
      int expectCell = myComponent.getConstraints().getCell(myVerticalGroup);
      for(int i=index-1; i >= 0; i--) {
        expectCell--;
        if (buttonsToGroup.get(i).getConstraints().getCell(myVerticalGroup) != expectCell) {
          buttonsToGroup.removeAll(buttonsToGroup.subList(0, i));
          break;
        }
      }
      expectCell = myComponent.getConstraints().getCell(myVerticalGroup);
      for(int i=index+1; i<buttonsToGroup.size(); i++) {
        expectCell++;
        if (buttonsToGroup.get(i).getConstraints().getCell(myVerticalGroup) != expectCell) {
          buttonsToGroup.removeAll(buttonsToGroup.subList(i, buttonsToGroup.size()-1));
          break;
        }
      }

      LOG.assertTrue(buttonsToGroup.size() > 1);
      GroupButtonsAction.groupButtons(myEditor, buttonsToGroup);
    }
  }
}