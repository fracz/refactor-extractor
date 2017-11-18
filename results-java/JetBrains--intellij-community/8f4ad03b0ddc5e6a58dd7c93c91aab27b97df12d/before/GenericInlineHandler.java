package com.intellij.refactoring.inline;

import com.intellij.codeInsight.TargetElementUtilBase;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageRefactoringSupport;
import com.intellij.lang.refactoring.InlineHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.ui.ConflictsDialog;
import com.intellij.util.containers.HashMap;
import com.intellij.util.containers.HashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author ven
 */
public class GenericInlineHandler {
  public static void invoke(final PsiElement element, final Editor editor, final InlineHandler languageSpecific) {
    final PsiReference invocationReference = TargetElementUtilBase.findReference(editor);
    final InlineHandler.Settings settings = languageSpecific.prepareInlineElement(element, editor, invocationReference != null);
    if (settings == null) return;

    final Collection<PsiReference> allReferences =
      settings.isOnlyOneReferenceToInline() ? Collections.singleton(invocationReference) : ReferencesSearch.search(element).findAll();
    final Map<Language, InlineHandler.Inliner> inliners = new HashMap<Language, InlineHandler.Inliner>();
    final Set<String> conflicts = new HashSet<String>();
    for (PsiReference ref : allReferences) {
      final Language language = ref.getElement().getLanguage();
      if (inliners.containsKey(language)) continue;

      final InlineHandler inlineHandler = LanguageRefactoringSupport.INSTANCE.forLanguage(language).getInlineHandler();
      if (inlineHandler == null) {
        conflicts.add("Cannot inline reference from " + language.getID());
      }
      else {
        final InlineHandler.Inliner inliner = inlineHandler.createInliner(element);
        if (inliner == null) {
          conflicts.add("Cannot inline reference from " + language.getID());
        }
        else {
          inliners.put(language, inliner);
        }
      }
    }

    for (PsiReference reference : allReferences) {
      collectConflicts(reference, element, inliners, conflicts);
    }

    final Project project = element.getProject();
    if (!conflicts.isEmpty()) {
      final ConflictsDialog conflictsDialog = new ConflictsDialog(project, conflicts);
      conflictsDialog.show();
      if (!conflictsDialog.isOK()) return;
    }

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        final String subj = element instanceof PsiNamedElement ? ((PsiNamedElement)element).getName() : "element";

        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
          public void run() {
            for (PsiReference reference : allReferences) {
              inlineReference(reference, element, inliners);
            }

            if (!settings.isOnlyOneReferenceToInline()) {
              languageSpecific.removeDefinition(element);
            }
          }
        }, RefactoringBundle.message("inline.command", subj), null);
      }
    });
  }

  private static void collectConflicts(final PsiReference reference,
                                       final PsiElement element,
                                       final Map<Language, InlineHandler.Inliner> inliners,
                                       final Set<String> conflicts) {
    final Language language = reference.getElement().getLanguage();
    final InlineHandler.Inliner inliner = inliners.get(language);
    if (inliner != null) {
      final Collection<String> refConflicts = inliner.getConflicts(reference, element);
      if (refConflicts != null) {
        conflicts.addAll(refConflicts);
      }
    }
  }

  private static void inlineReference(final PsiReference reference,
                                      final PsiElement element,
                                      final Map<Language, InlineHandler.Inliner> inliners) {
    final Language language = reference.getElement().getLanguage();
    final InlineHandler.Inliner inliner = inliners.get(language);
    if (inliner != null) {
      inliner.inlineReference(reference, element);
    }
  }

}