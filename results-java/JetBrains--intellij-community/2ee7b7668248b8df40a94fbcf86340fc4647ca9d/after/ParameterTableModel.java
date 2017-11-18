package com.intellij.refactoring.changeSignature;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.refactoring.ui.RowEditableTableModel;
import com.intellij.refactoring.RefactoringBundle;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author ven
 */
class ParameterTableModel extends AbstractTableModel implements RowEditableTableModel {
  private static final Logger LOG = Logger.getInstance("#com.intellij.refactoring.changeSignature.ParameterTableModel");
  private List<ParameterInfo> myParameterInfos;
  private List<PsiTypeCodeFragment> myTypeCodeFraments;
  private List<PsiCodeFragment> myDefaultValuesCodeFragments;
  private final PsiElement myContext;
  private final ChangeSignatureDialog myDialog;
  static final String ANY_VAR_COLUMN_NAME = RefactoringBundle.message("column.name.any.var");

  public ParameterTableModel(PsiElement context, ChangeSignatureDialog dialog) {
    myContext = context;
    myDialog = dialog;
  }

  public Class getColumnClass(int columnIndex) {
    if (columnIndex == 3) return Boolean.class;
    return super.getColumnClass(columnIndex);
  }

  public List<PsiTypeCodeFragment> getCodeFraments() {
    return Collections.unmodifiableList(myTypeCodeFraments);
  }

  public List<PsiCodeFragment> getDefaultValueFraments() {
    return Collections.unmodifiableList(myDefaultValuesCodeFragments);
  }

  public ParameterInfo[] getParameters() {
    return myParameterInfos.toArray(new ParameterInfo[myParameterInfos.size()]);
  }


  public void addRow() {
    ParameterInfo info = new ParameterInfo(-1);
    myParameterInfos.add(info);
    myTypeCodeFraments.add(createParameterTypeCodeFragment("", myContext));
    myDefaultValuesCodeFragments.add(createDefaultValueCodeFragment("", null));
    fireTableRowsInserted(myParameterInfos.size() - 1, myParameterInfos.size() - 1);
  }

  public void removeRow(int index) {
    myParameterInfos.remove(index);
    myTypeCodeFraments.remove(index);
    myDefaultValuesCodeFragments.remove(index);
    fireTableRowsDeleted(index, index);
  }

  public void exchangeRows(int index1, int index2) {
    Collections.swap(myParameterInfos, index1, index2);
    Collections.swap(myTypeCodeFraments, index1, index2);
    Collections.swap(myDefaultValuesCodeFragments, index1, index2);
    if (index1 < index2) {
      fireTableRowsUpdated(index1, index2);
    }
    else {
      fireTableRowsUpdated(index2, index1);
    }
  }

  public int getRowCount() {
    return myParameterInfos.size();
  }

  public int getColumnCount() {
    return 4;
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    ParameterInfo info = myParameterInfos.get(rowIndex);
    switch (columnIndex) {
      case 0:
        return myTypeCodeFraments.get(rowIndex);
      case 1:
        return info.getName();
      case 2:
        return myDefaultValuesCodeFragments.get(rowIndex);
      case 3:
        return Boolean.valueOf(info.useAnySingleVariable);

      default:
        throw new IllegalArgumentException();
    }
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (rowIndex < 0 || rowIndex >= myParameterInfos.size()) return;
    String s = aValue instanceof String ? (String)aValue : null;
    if (s == null) s = "";
    s = s.trim();
    ParameterInfo info = myParameterInfos.get(rowIndex);
    switch (columnIndex) {
      case 0:
        //info.setType();
        break;

      case 1:
        info.setName(s);
        break;

      case 2:
        break;

      case 3:
        info.setUseAnySingleVariable(((Boolean) aValue).booleanValue());
        break;

      default:
        throw new IllegalArgumentException();
    }
    fireTableCellUpdated(rowIndex, columnIndex);
  }

  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return RefactoringBundle.message("column.name.type");
      case 1:
        return RefactoringBundle.message("column.name.name");
      case 2:
        return RefactoringBundle.message("column.name.default.value");
      case 3:
        return ANY_VAR_COLUMN_NAME;
      default:
        throw new IllegalArgumentException();
    }
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 0:
      case 1:
        return true;

      case 2:
        {
          final PsiType typeByRow = getTypeByRow(rowIndex);
          final boolean isEllipsis = typeByRow instanceof PsiEllipsisType;
          ParameterInfo info = myParameterInfos.get(rowIndex);
          return !isEllipsis && info.oldParameterIndex < 0;
        }

      case 3:
        {
          if (myDialog.isGenerateDelegate()) return false;

          final PsiType typeByRow = getTypeByRow(rowIndex);
          final boolean isEllipsis = typeByRow instanceof PsiEllipsisType;
          return !isEllipsis && myParameterInfos.get(rowIndex).oldParameterIndex < 0;
        }

      default:
        throw new IllegalArgumentException();
    }
  }

  private PsiCodeFragment createDefaultValueCodeFragment(final String expressionText, final PsiType expectedType) {
    PsiExpressionCodeFragment codeFragment = myContext.getManager().getElementFactory().createExpressionCodeFragment(expressionText, null, expectedType, true);
    codeFragment.setEverythingAcessible(true);
    return codeFragment;
  }

  PsiType getTypeByRow(int row) {
    Object typeValueAt = getValueAt(row, 0);
    LOG.assertTrue(typeValueAt instanceof PsiTypeCodeFragment);
    PsiType type;
    try {
      type = ((PsiTypeCodeFragment)typeValueAt).getType();
    }
    catch (PsiTypeCodeFragment.TypeSyntaxException e1) {
      type = null;
    }
    catch (PsiTypeCodeFragment.NoTypeException e1) {
      type = null;
    }
    return type;
  }

  public void setParameterInfos(List<ParameterInfo> parameterInfos, PsiElement context) {
    myParameterInfos = parameterInfos;
    myTypeCodeFraments = new ArrayList<PsiTypeCodeFragment>(parameterInfos.size());
    myDefaultValuesCodeFragments = new ArrayList<PsiCodeFragment>(parameterInfos.size());
    for (ParameterInfo parameterInfo : parameterInfos) {
      final PsiTypeCodeFragment typeCodeFragment = createParameterTypeCodeFragment(parameterInfo.getTypeText(), context);
      parameterInfo.getTypeWrapper().addImportsTo(typeCodeFragment);
      myTypeCodeFraments.add(typeCodeFragment);
      myDefaultValuesCodeFragments.add(createDefaultValueCodeFragment(parameterInfo.defaultValue, null));
    }
  }

  PsiTypeCodeFragment createParameterTypeCodeFragment(final String typeText, PsiElement context) {
    return myContext.getManager().getElementFactory().createTypeCodeFragment(
        typeText, context, false, true, true
      );
  }
}