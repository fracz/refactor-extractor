package org.jetbrains.jet.plugin.actions;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.daemon.QuickFixBundle;
import com.intellij.codeInsight.daemon.impl.actions.AddImportAction;
import com.intellij.codeInsight.hint.QuestionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.plugin.quickfix.ImportClassHelper;

import javax.swing.*;
import java.util.List;

/**
 * Automatically adds import directive to the file for resolving reference.
 * Based on {@link AddImportAction}
 *
 * @author Nikolay Krasko
 */
public class JetAddImportAction implements QuestionAction {

    private final Project myProject;
    private final Editor myEditor;
    private final PsiElement myElement;
    private final List<String> possibleImports;

    /**
     * @param project Project where action takes place.
     * @param editor Editor where modification should be done.
     * @param element Element with unresolved reference.
     * @param imports Variants for resolution.
     */
    public JetAddImportAction(
            @NotNull Project project,
            @NotNull Editor editor,
            @NotNull PsiElement element,
            @NotNull Iterable<String> imports
    ) {
        myProject = project;
        myEditor = editor;
        myElement = element;
        possibleImports = Lists.newArrayList(imports);
    }

    @Override
    public boolean execute() {
        PsiDocumentManager.getInstance(myProject).commitAllDocuments();

        if (!myElement.isValid()){
            return false;
        }

        // TODO: Validate resolution variants. See AddImportAction.execute()

        if (possibleImports.size() == 1){
            addImport(myElement, myProject, possibleImports.get(0));
        }
        else{
            chooseClassAndImport();
        }

        return true;
    }

    protected BaseListPopupStep getImportSelectionPopup() {
        return new BaseListPopupStep<String>(QuickFixBundle.message("class.to.import.chooser.title"), possibleImports) {
            @Override
            public boolean isAutoSelectionEnabled() {
                return false;
            }

            @Override
            public PopupStep onChosen(String selectedValue, boolean finalChoice) {
                if (selectedValue == null) {
                    return FINAL_CHOICE;
                }

                if (finalChoice) {
                    addImport(myElement, myProject, selectedValue);
                    return FINAL_CHOICE;
                }

                List<String> toExclude = AddImportAction.getAllExcludableStrings(selectedValue);

                return new BaseListPopupStep<String>(null, toExclude) {
                    @NotNull
                    @Override
                    public String getTextFor(String value) {
                        return "Exclude '" + value + "' from auto-import";
                    }

                    @Override
                    public PopupStep onChosen(String selectedValue, boolean finalChoice) {
                        if (finalChoice) {
                            AddImportAction.excludeFromImport(myProject, selectedValue);
                        }

                        return super.onChosen(selectedValue, finalChoice);
                    }
                };
            }

            @Override
            public boolean hasSubstep(String selectedValue) {
                return true;
            }

            @NotNull
            @Override
            public String getTextFor(String value) {
                return value;
            }

            @Override
            public Icon getIconFor(String aValue) {
                return PlatformIcons.CLASS_ICON;
            }
        };
    }

    protected void addImport(final PsiElement element, final Project project, final String selectedImport) {
        PsiDocumentManager.getInstance(project).commitAllDocuments();

        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        // TODO: See {@link com.intellij.codeInsight.daemon.impl.actions.AddImportAction#_addImport} for more ideas.
                        // TODO: Optimize imports
                        PsiFile file = element.getContainingFile();
                        if (!(file instanceof JetFile)) return;
                        ImportClassHelper.addImportDirective(
                                selectedImport,
                                (JetFile)file
                        );
                    }
                });
            }
        }, QuickFixBundle.message("add.import"), null);
    }

    private void chooseClassAndImport() {
        JBPopupFactory.getInstance().createListPopup(getImportSelectionPopup()).showInBestPositionFor(myEditor);
    }
}