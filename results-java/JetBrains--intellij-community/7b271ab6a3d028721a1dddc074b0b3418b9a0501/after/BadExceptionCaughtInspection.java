/*
 * Copyright 2003-2011 Dave Griffith, Bas Leijdekkers
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
package com.siyeh.ig.errorhandling;

import com.intellij.codeInspection.ui.ListTable;
import com.intellij.codeInspection.ui.ListWrappingTableModel;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiCatchSection;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeElement;
import com.intellij.ui.ScrollPaneFactory;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspection;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ui.ExternalizableStringSet;
import com.siyeh.ig.ui.UiUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

public class BadExceptionCaughtInspection extends BaseInspection {

    /** @noinspection PublicField*/
    public String exceptionsString = "";

    /** @noinspection PublicField*/
    public final ExternalizableStringSet exceptions =
            new ExternalizableStringSet(
                    "java.lang.NullPointerException",
                    "java.lang.IllegalMonitorStateException",
                    "java.lang.ArrayIndexOutOfBoundsException"
            );

    public BadExceptionCaughtInspection() {
        if (exceptionsString.length() != 0) {
            exceptions.clear();
            final List<String> strings =
                    StringUtil.split(exceptionsString, ",");
            for (String string : strings) {
                exceptions.add(string);
            }
            exceptionsString = "";
        }
    }

    @NotNull
    public String getID() {
        return "ProhibitedExceptionCaught";
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "bad.exception.caught.display.name");
    }

    @Override
    @NotNull
    public String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "bad.exception.caught.problem.descriptor");
    }

    @Override
    public JComponent createOptionsPanel() {
        final JComponent panel = new JPanel(new GridBagLayout());

        final ListTable table =
                new ListTable(new ListWrappingTableModel(exceptions,
                        InspectionGadgetsBundle.message(
                                "ignored.io.resource.types")));
        final JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(table);
        final FontMetrics fontMetrics = table.getFontMetrics(table.getFont());
        scrollPane.setPreferredSize(new Dimension(0, fontMetrics.getHeight() * 7));
        scrollPane.setMinimumSize(new Dimension(0, fontMetrics.getHeight() * 3));

        final ActionToolbar toolbar =
                UiUtils.createAddRemoveTreeClassChooserToolbar(table,
                        InspectionGadgetsBundle.message(
                                "exception.class.column.name"),
                        "java.lang.Throwable");

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets.left = 4;
        constraints.insets.right = 4;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(toolbar.getComponent(), constraints);

        constraints.gridy = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        panel.add(scrollPane, constraints);

        return panel;
    }

    @Override
    public BaseInspectionVisitor buildVisitor() {
        return new BadExceptionCaughtVisitor();
    }

    private class BadExceptionCaughtVisitor extends BaseInspectionVisitor {

        @Override
        public void visitCatchSection(PsiCatchSection section) {
            super.visitCatchSection(section);
            final PsiParameter parameter = section.getParameter();
            if(parameter == null) {
                return;
            }
            final PsiType type = parameter.getType();
            final String text = type.getCanonicalText();
            if (text == null) {
                return;
            }
            if (!exceptions.contains(text)) {
                return;
            }
            final PsiTypeElement typeElement = parameter.getTypeElement();
            if (typeElement == null) {
                return;
            }
            registerError(typeElement);
        }
    }
}