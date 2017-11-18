/*
 * Copyright 2003-2006 Dave Griffith, Bas Leijdekkers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siyeh.ig.logging;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.MethodInspection;
import com.siyeh.ig.RegExInputVerifier;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.*;

public class PublicMethodWithoutLoggingInspection extends MethodInspection{

    /**
     * @noinspection PublicField
     */
    public String loggerClassName = "java.util.logging.Logger";

    public String getDisplayName(){
        return InspectionGadgetsBundle.message(
                "public.method.without.logging.display.name");
    }

    public String getGroupDisplayName(){
        return GroupNames.LOGGING_GROUP_NAME;
    }

    @NotNull
    public String buildErrorString(Object... infos){
        return InspectionGadgetsBundle.message(
                "public.method.without.logging.problem.descriptor");
    }

    public JComponent createOptionsPanel(){
        final GridBagLayout layout = new GridBagLayout();
        final JPanel panel = new JPanel(layout);

        final JLabel classNameLabel = new JLabel(
                InspectionGadgetsBundle.message("logger.name.option"));
        classNameLabel.setHorizontalAlignment(SwingConstants.TRAILING);

        final JTextField loggerClassNameField = new JTextField();
        final Font panelFont = panel.getFont();
        loggerClassNameField.setFont(panelFont);
        loggerClassNameField.setText(loggerClassName);
        loggerClassNameField.setColumns(100);
        loggerClassNameField.setInputVerifier(new RegExInputVerifier());

        final DocumentListener listener = new DocumentListener(){
            public void changedUpdate(DocumentEvent e){
                textChanged();
            }

            public void insertUpdate(DocumentEvent e){
                textChanged();
            }

            public void removeUpdate(DocumentEvent e){
                textChanged();
            }

            private void textChanged(){
                loggerClassName = loggerClassNameField.getText();
            }
        };
        final Document loggerClassNameDocument =
                loggerClassNameField.getDocument();
        loggerClassNameDocument.addDocumentListener(listener);

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(classNameLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        panel.add(loggerClassNameField, constraints);

        return panel;
    }

    public BaseInspectionVisitor buildVisitor(){
        return new PublicMethodWithoutLoggingVisitor();
    }

    private class PublicMethodWithoutLoggingVisitor
            extends BaseInspectionVisitor{

        public void visitMethod(@NotNull PsiMethod method){
            //no drilldown
            if(!method.hasModifierProperty(PsiModifier.PUBLIC)){
                return;
            }
            if(method.getBody() == null){
                return;
            }
            if(method.isConstructor()){
                return;
            }
            if(PropertyUtil.isSimplePropertyAccessor(method)){
                return;
            }
            if(containsLoggingCall(method)){
                return;
            }
            registerMethodError(method);
        }

        private boolean containsLoggingCall(PsiMethod method){
            final ContainsLoggingCallVisitor visitor =
                    new ContainsLoggingCallVisitor();
            method.accept(visitor);
            return visitor.containsLoggingCall();
        }
    }

    private class ContainsLoggingCallVisitor
            extends PsiRecursiveElementVisitor{

        private boolean containsLoggingCall = false;

        public void visitElement(@NotNull PsiElement element){
            if(containsLoggingCall){
                super.visitElement(element);
            }
        }

        public void visitMethodCallExpression(
                @NotNull PsiMethodCallExpression expression){
            if(containsLoggingCall){
                return;
            }
            super.visitMethodCallExpression(expression);
            final PsiMethod method = expression.resolveMethod();
            if(method == null){
                return;
            }
            final PsiClass containingClass = method.getContainingClass();
            if(containingClass == null){
                return;
            }
            final String containingClassName =
                    containingClass.getQualifiedName();
            if(containingClassName == null){
                return;
            }
            if(containingClassName.equals(loggerClassName)){
                containsLoggingCall = true;
            }
        }

        public boolean containsLoggingCall(){
            return containsLoggingCall;
        }
    }
}