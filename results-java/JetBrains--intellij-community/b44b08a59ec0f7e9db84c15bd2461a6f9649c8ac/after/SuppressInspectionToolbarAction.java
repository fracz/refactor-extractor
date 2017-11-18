/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.codeInspection.ui;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInsight.daemon.impl.AddNoInspectionDocTagAction;
import com.intellij.codeInsight.daemon.impl.AddSuppressWarningsAnnotationAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.ex.InspectionTool;
import com.intellij.codeInspection.reference.RefElement;
import com.intellij.codeInspection.reference.RefEntity;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.impl.ModuleUtil;
import com.intellij.openapi.projectRoots.ProjectJdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

/**
 * User: anna
 * Date: 11-Jan-2006
 */
class SuppressInspectionToolbarAction extends AnAction {
  private InspectionResultsView myView;
  private static final Logger LOG = Logger.getInstance("com.intellij.codeInspection.ui.SuppressInspectionToolbarAction");

  public SuppressInspectionToolbarAction(final InspectionResultsView view) {
    super(InspectionsBundle.message("suppress.inspection.family"), InspectionsBundle.message("suppress.inspection.family"),
          IconLoader.getIcon("/general/inspectionsOff.png"));
    myView = view;
  }

  public void actionPerformed(AnActionEvent e) {
    final InspectionTool selectedTool = myView.getTree().getSelectedTool();
    assert selectedTool != null;
    getSuppressAction(selectedTool, myView.getTree().getSelectionPaths(), HighlightDisplayKey.find(selectedTool.getShortName()).getID())
      .actionPerformed(e);
  }

  public void update(AnActionEvent e) {
    if (!myView.isSingleToolInSelection()) {
      e.getPresentation().setEnabled(false);
      return;
    }
    final InspectionTool selectedTool = myView.getTree().getSelectedTool();
    assert selectedTool != null;
    final HighlightDisplayKey key = HighlightDisplayKey.find(selectedTool.getShortName());
    if (key == null) {
      e.getPresentation().setEnabled(false);
      return;
    }
    getSuppressAction(selectedTool, myView.getTree().getSelectionPaths(), HighlightDisplayKey.find(selectedTool.getShortName()).getID())
      .update(e);
  }


  private AnAction getSuppressAction(final InspectionTool tool, final TreePath[] selectionPaths, final String id) {
    final AnAction suppressAction = new AnAction(InspectionsBundle.message("inspection.quickfix.suppress", tool.getDisplayName())) {
      public void actionPerformed(AnActionEvent e) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          public void run() {
            PsiDocumentManager.getInstance(myView.getProject()).commitAllDocuments();
            CommandProcessor.getInstance().executeCommand(myView.getProject(), new Runnable() {
              public void run() {
                for (TreePath treePath : selectionPaths) {
                  final InspectionTreeNode node = (InspectionTreeNode)treePath.getLastPathComponent();
                  final List<RefElement> elementsToSuppress = myView.getTree().getElementsToSuppressInSubTree(node);
                  for (final RefElement refElement : elementsToSuppress) {
                    final PsiElement element = refElement.getElement();
                    final IntentionAction action = getCorrectIntentionAction(tool.getDisplayName(), id, element);
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                      public void run() {
                        try {
                          action.invoke(myView.getProject(), null, refElement.getElement().getContainingFile());
                        }
                        catch (IncorrectOperationException e1) {
                          LOG.error(e1);
                        }
                      }
                    });
                  }
                  final List<RefEntity> elementsToIgnore = new ArrayList<RefEntity>();
                  InspectionResultsView.traverseRefElements(node, elementsToIgnore);
                  for (RefEntity element : elementsToIgnore) {
                    tool.ignoreElement(element);
                  }
                }
                myView.update();
              }
            }, InspectionsBundle.message("inspection.quickfix.suppress"), null);
          }
        });
      }

      public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(true);
        for (TreePath treePath : selectionPaths) {
          final InspectionTreeNode node = (InspectionTreeNode)treePath.getLastPathComponent();
          final List<RefElement> elementsToSuppress = myView.getTree().getElementsToSuppressInSubTree(node);
          for (RefElement refElement : elementsToSuppress) {
            final PsiElement element = refElement.getElement();
            if (element instanceof PsiFile) continue;
            if (element == null || !element.isValid()) continue;
            final PsiFile file = element.getContainingFile();
            final IntentionAction action = getCorrectIntentionAction(tool.getDisplayName(), id, element);
            if (action.isAvailable(myView.getProject(), null, file)) {
              e.getPresentation().setEnabled(true);
              return;
            }
          }
        }
        e.getPresentation().setEnabled(false);
      }
    };
    return suppressAction;
  }


  private static class SuppressWarningAction extends AddSuppressWarningsAnnotationAction {
    public SuppressWarningAction(final String displayName, final String ID, final PsiElement context) {
      super(displayName, ID, context);
    }

    @Nullable
    protected PsiModifierListOwner getContainer() {
      if (!(myContext.getContainingFile().getLanguage() instanceof JavaLanguage) || myContext instanceof PsiFile) {
        return null;
      }
      PsiElement container = myContext;
      while (container instanceof PsiClassInitializer || container instanceof PsiAnonymousClass || container instanceof PsiTypeParameter) {
        container = PsiTreeUtil.getParentOfType(container, PsiMember.class);
      }
      return (PsiModifierListOwner)container;
    }
  }

  private static class SuppressDocCommentAction extends AddNoInspectionDocTagAction {

    public SuppressDocCommentAction(final String displayName, final String ID, final PsiElement context) {
      super(displayName, ID, context);
    }

    @Nullable
    protected PsiDocCommentOwner getContainer() {
      if (!(myContext.getContainingFile().getLanguage() instanceof JavaLanguage) || myContext instanceof PsiFile) {
        return null;
      }
      PsiElement container = myContext;
      while (container instanceof PsiTypeParameter) {
        container = PsiTreeUtil.getParentOfType(container, PsiDocCommentOwner.class);
      }
      return (PsiDocCommentOwner)container;
    }
  }

  static IntentionAction getCorrectIntentionAction(String displayName, String id, PsiElement context) {
    boolean isSuppressWarnings = false;
    final Module module = ModuleUtil.findModuleForPsiElement(context);
    if (module != null) {
      final ProjectJdk jdk = ModuleRootManager.getInstance(module).getJdk();
      if (jdk != null) {
        isSuppressWarnings = DaemonCodeAnalyzerSettings.getInstance().SUPPRESS_WARNINGS && jdk.getVersionString().indexOf("1.5") > 0 &&
                             LanguageLevel.JDK_1_5.compareTo(context.getManager().getEffectiveLanguageLevel()) <= 0;
      }
    }
    if (isSuppressWarnings) {
      return new SuppressWarningAction(displayName, id, context);
    }
    return new SuppressDocCommentAction(displayName, id, context);
  }

  @Nullable
  public static AnAction getSuppressAction( final RefElement refElement,
                                            final InspectionTool tool,
                                            final InspectionResultsView view){
      final HighlightDisplayKey key = HighlightDisplayKey.find(tool.getShortName());
      if (key != null) {
        final IntentionAction action = SuppressInspectionToolbarAction.getCorrectIntentionAction(tool.getDisplayName(), key.getID(), refElement.getElement());
        final PsiFile file = refElement.getElement().getContainingFile();
        if (action.isAvailable(view.getProject(), null, file)) {
          return new AnAction(action.getText()) {
            public void actionPerformed(AnActionEvent e) {
              final List<RefEntity> elementsToSuppress = new ArrayList<RefEntity>();
              InspectionResultsView.traverseRefElements((InspectionTreeNode)view.getTree().getSelectionPath().getLastPathComponent(), elementsToSuppress);
              invokeSuppressAction(action, refElement, tool, elementsToSuppress, view);
            }
          };
        }
      }
      return null;
    }

   private static void invokeSuppressAction(final IntentionAction action,
                                            final RefElement element,
                                            final InspectionTool tool,
                                            final List<RefEntity> elementsToSuppress,
                                            final InspectionResultsView view) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          public void run() {
            PsiDocumentManager.getInstance(view.getProject()).commitAllDocuments();
            CommandProcessor.getInstance().executeCommand(view.getProject(), new Runnable() {
              public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                  public void run() {
                    try {
                      action.invoke(view.getProject(), null, element.getElement().getContainingFile());
                      for (RefEntity refElement : elementsToSuppress) {
                        tool.ignoreElement(refElement);
                      }
                      view.update();
                    }
                    catch (IncorrectOperationException e1) {
                      LOG.error(e1);
                    }
                  }
                });
              }
            }, action.getText(), null);
          }
        });
      }
    });
  }
}