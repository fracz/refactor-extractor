package com.intellij.uiDesigner;

import com.intellij.ExtensionPoints;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.uiDesigner.designSurface.GuiEditor;
import com.intellij.uiDesigner.lw.IComponent;
import com.intellij.uiDesigner.lw.IContainer;
import com.intellij.uiDesigner.lw.IRootContainer;
import com.intellij.uiDesigner.quickFixes.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;

/**
 * @author Anton Katilin
 * @author Vladimir Kondratyev
 */
public final class ErrorAnalyzer {
  private static final Logger LOG = Logger.getInstance("#com.intellij.uiDesigner.ErrorAnalyzer");

  /**
   * Value {@link ErrorInfo}
   */
  @NonNls
  public static final String CLIENT_PROP_CLASS_TO_BIND_ERROR = "classToBindError";
  /**
   * Value {@link ErrorInfo}
   */
  @NonNls
  public static final String CLIENT_PROP_BINDING_ERROR = "bindingError";

  @NonNls public static final String CLIENT_PROP_ERROR_ARRAY = "errorArray";

  private ErrorAnalyzer() {
  }

  public static void analyzeErrors(final GuiEditor editor, final IRootContainer rootContainer){
    analyzeErrors(editor.getModule(), editor.getFile(), editor, rootContainer);
  }

  /**
   * @param editor if null, no quick fixes are created. This is used in form to source compiler.
   */
  public static void analyzeErrors(
    @NotNull final Module module,
    @NotNull final VirtualFile formFile,
    @Nullable final GuiEditor editor,
    @NotNull final IRootContainer rootContainer
  ){
    // 1. Validate class to bind
    final String classToBind = rootContainer.getClassToBind();
    final PsiClass psiClass;
    if(classToBind != null){
      psiClass = FormEditingUtil.findClassToBind(module, classToBind);
      if(psiClass == null){
        final QuickFix[] fixes = editor != null ? new QuickFix[]{new CreateClassToBindFix(editor, classToBind)} : QuickFix.EMPTY_ARRAY;
        final ErrorInfo errorInfo = new ErrorInfo(null, UIDesignerBundle.message("error.class.does.not.exist", classToBind),
                                                  fixes);
        rootContainer.putClientProperty(CLIENT_PROP_CLASS_TO_BIND_ERROR, errorInfo);
      }
      else{
        rootContainer.putClientProperty(CLIENT_PROP_CLASS_TO_BIND_ERROR, null);
      }
    }
    else{
      psiClass = null;
    }

    // 2. Validate bindings to fields
    // field name -> error message
    final ArrayList<String> usedBindings = new ArrayList<String>(); // for performance reasons
    final HashMap<String, PsiType> className2Type = new HashMap<String,PsiType>(); // for performance reasons
    FormEditingUtil.iterate(
      rootContainer,
      new FormEditingUtil.ComponentVisitor<IComponent>() {
        public boolean visit(final IComponent component) {
          // Reset previous error (if any)
          component.putClientProperty(CLIENT_PROP_BINDING_ERROR, null);

          final String binding = component.getBinding();
          if(binding == null){
            return true;
          }

          // a. Check that field exists and field is not static
          if(psiClass != null){
            final PsiField[] fields = psiClass.getFields();
            PsiField field = null;
            for(int i = fields.length - 1; i >=0 ; i--){
              if(binding.equals(fields[i].getName())){
                field = fields[i];
                break;
              }
            }
            if(field == null){
              final QuickFix[] fixes = editor != null ? new QuickFix[]{
                new CreateFieldFix(editor, psiClass, component.getComponentClassName(), binding)} :
                                                                                                  QuickFix.EMPTY_ARRAY;
              component.putClientProperty(
               CLIENT_PROP_BINDING_ERROR,
               new ErrorInfo(
                 null, UIDesignerBundle.message("error.no.field.in.class", binding, classToBind),
                 fixes
               )
              );
              return true;
            }
            else if(field.hasModifierProperty(PsiModifier.STATIC)){
              component.putClientProperty(
                CLIENT_PROP_BINDING_ERROR,
                new ErrorInfo(
                  null, UIDesignerBundle.message("error.cant.bind.to.static", binding),
                  QuickFix.EMPTY_ARRAY
                )
              );
              return true;
            }

            // Check that field has correct fieldType
            try {
              final PsiType componentType;
              final String className = component.getComponentClassName().replace('$', '.'); // workaround for PSI
              if(className2Type.containsKey(className)){
                componentType = className2Type.get(className);
              }
              else{
                componentType = PsiManager.getInstance(module.getProject()).getElementFactory().createTypeFromText(
                  className,
                  null
                );
              }
              final PsiType fieldType = field.getType();
              if(fieldType != null && componentType != null && !fieldType.isAssignableFrom(componentType)){
                final QuickFix[] fixes = editor != null ? new QuickFix[]{
                  new ChangeFieldTypeFix(editor, field, componentType)
                } : QuickFix.EMPTY_ARRAY;
                component.putClientProperty(
                  CLIENT_PROP_BINDING_ERROR,
                  new ErrorInfo(
                    null, UIDesignerBundle.message("error.bind.incompatible.types", fieldType.getPresentableText(), className),
                    fixes
                  )
                );
              }
            }
            catch (IncorrectOperationException e) {
            }
          }

          // b. Check that binding is unique
          if(usedBindings.contains(binding)){
            // TODO[vova] implement
            component.putClientProperty(
              CLIENT_PROP_BINDING_ERROR,
              new ErrorInfo(
                null, UIDesignerBundle.message("error.binding.already.exists", binding),
                QuickFix.EMPTY_ARRAY
              )
            );
            return true;
          }

          usedBindings.add(binding);

          return true;
        }
      }
    );

    // Check that there are no panels in XY with children
    FormEditingUtil.iterate(
      rootContainer,
      new FormEditingUtil.ComponentVisitor<IComponent>() {
        public boolean visit(final IComponent component) {
          // Clear previous error (if any)
          component.putClientProperty(CLIENT_PROP_ERROR_ARRAY, null);

          if(!(component instanceof IContainer)){
            return true;
          }

          final IContainer container = (IContainer)component;
          if(container instanceof IRootContainer){
            final IRootContainer rootContainer = (IRootContainer)container;
            if(rootContainer.getComponentCount() > 1){
              // TODO[vova] implement
              putError(component, new ErrorInfo(
                null, UIDesignerBundle.message("error.multiple.toplevel.components"),
                QuickFix.EMPTY_ARRAY
              ));
            }
          }
          else if(container.isXY() && container.getComponentCount() > 0){
            // TODO[vova] implement
            putError(component, new ErrorInfo(
                null, UIDesignerBundle.message("error.panel.not.laid.out"),
                QuickFix.EMPTY_ARRAY
              )
            );
          }
          return true;
        }
      }
    );

    try {
      // Run inspections for form elements
      final PsiFile formPsiFile = PsiManager.getInstance(module.getProject()).findFile(formFile);
      if (formPsiFile != null && rootContainer instanceof RadRootContainer) {
        final List<FormInspectionTool> formInspectionTools = new ArrayList<FormInspectionTool>();
        for(Object object: Extensions.getRootArea().getExtensionPoint(ExtensionPoints.FORM_INSPECTION_TOOL).getExtensions()) {
          final FormInspectionTool formInspectionTool = (FormInspectionTool)object;
          if (formInspectionTool.isActive(formPsiFile)) {
            formInspectionTools.add(formInspectionTool);
          }
        }

        if (formInspectionTools.size() > 0 && editor != null) {
          FormEditingUtil.iterate(
            rootContainer,
            new FormEditingUtil.ComponentVisitor<RadComponent>() {
              public boolean visit(final RadComponent component) {
                for(FormInspectionTool tool: formInspectionTools) {
                  ErrorInfo[] errorInfos = tool.checkComponent(editor, component);
                  if (errorInfos != null) {
                    ArrayList<ErrorInfo> errorList = (ArrayList<ErrorInfo>)component.getClientProperty(CLIENT_PROP_ERROR_ARRAY);
                    if (errorList == null) {
                      errorList = new ArrayList<ErrorInfo>();
                      component.putClientProperty(CLIENT_PROP_ERROR_ARRAY, errorList);
                    }
                    Collections.addAll(errorList, errorInfos);
                  }
                }
                return true;
              }
            }
          );
        }
      }
    }
    catch (Exception e) {
      LOG.error(e);
    }
  }

  private static void putError(final IComponent component, final ErrorInfo errorInfo) {
    ArrayList<ErrorInfo> errorList = (ArrayList<ErrorInfo>)component.getClientProperty(CLIENT_PROP_ERROR_ARRAY);
    if (errorList == null) {
      errorList = new ArrayList<ErrorInfo>();
      component.putClientProperty(CLIENT_PROP_ERROR_ARRAY, errorList);
    }

    errorList.add(errorInfo);
  }

  /**
   * @return first ErrorInfo for the specified component. If component doesn't contain
   * any error then the method returns <code>null</code>.
   */
  @Nullable
  public static ErrorInfo getErrorForComponent(@NotNull final IComponent component){
    // Check bind to class errors
    {
      final ErrorInfo errorInfo = (ErrorInfo)component.getClientProperty(CLIENT_PROP_CLASS_TO_BIND_ERROR);
      if(errorInfo != null){
        return errorInfo;
      }
    }

    // Check binding errors
    {
      final ErrorInfo error = (ErrorInfo)component.getClientProperty(CLIENT_PROP_BINDING_ERROR);
      if(error != null){
        return error;
      }
    }

    // General error
    {
      final ArrayList<ErrorInfo> errorInfo = (ArrayList<ErrorInfo>)component.getClientProperty(CLIENT_PROP_ERROR_ARRAY);
      if(errorInfo != null && errorInfo.size() > 0){
        return errorInfo.get(0);
      }
    }

    return null;
  }
}