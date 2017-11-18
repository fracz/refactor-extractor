/*
 * User: anna
 * Date: 14-May-2009
 */
package com.intellij.profile.codeInspection.ui.actions;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.codeInspection.ex.Descriptor;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.codeInspection.ex.ScopeToolState;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.packageDependencies.DefaultScopesProvider;
import com.intellij.profile.codeInspection.ui.InspectionConfigTreeNode;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.Icons;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.*;

public class AddScopeAction extends AnAction {
  private final Tree myTree;
  private final InspectionProfileImpl mySelectedProfile;
  private static final Logger LOG = Logger.getInstance("#" + AddScopeAction.class.getName());

  public AddScopeAction(Tree tree, InspectionProfileImpl profile) {
    super("Add Scope", "Add Scope", Icons.ADD_ICON);
    myTree = tree;
    mySelectedProfile = profile;
    registerCustomShortcutSet(CommonShortcuts.INSERT, myTree);
  }

  @Override
  public void update(AnActionEvent e) {
    final Presentation presentation = e.getPresentation();
    presentation.setEnabled(false);
    if (mySelectedProfile == null) return;
    final Project project = PlatformDataKeys.PROJECT.getData(e.getDataContext());
    if (project == null) return;
    final InspectionConfigTreeNode[] nodes = myTree.getSelectedNodes(InspectionConfigTreeNode.class, null);
    if (nodes.length > 0) {
      final InspectionConfigTreeNode node = nodes[0];
      final Descriptor descriptor = node.getDesriptor();
      if (descriptor != null && node.getScope() == null && !getAvailableScopes(descriptor, project).isEmpty()) {
        presentation.setEnabled(true);
      }
    }
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    final InspectionConfigTreeNode[] nodes = myTree.getSelectedNodes(InspectionConfigTreeNode.class, null);
    final InspectionConfigTreeNode node = nodes[0];
    final Descriptor descriptor = node.getDesriptor();
    LOG.assertTrue(descriptor != null);
    final Project project = PlatformDataKeys.PROJECT.getData(e.getDataContext());
    final InspectionProfileEntry tool = descriptor.getTool(); //copy
    final List<String> availableScopes = getAvailableScopes(descriptor, project);

    final int idx = Messages.showChooseDialog(myTree, "Scope:", "Choose Scope",
                                              availableScopes.toArray(new String[availableScopes.size()]), availableScopes.get(0), Messages.getQuestionIcon());
    if (idx == -1) return;
    final ScopeToolState scopeToolState = mySelectedProfile.addScope(tool, NamedScopesHolder.getScope(project, availableScopes.get(idx)),
                                                                     descriptor.getLevel(), tool.isEnabledByDefault());
    final Descriptor addedDescriptor = new Descriptor(scopeToolState, mySelectedProfile);
    if (node.getChildCount() == 0) {
      node.add(new InspectionConfigTreeNode(descriptor, DefaultScopesProvider.getAllScope(), true, descriptor.isEnabled(), true, false));
    }
    node.insert(new InspectionConfigTreeNode(addedDescriptor, scopeToolState.getScope(), tool.isEnabledByDefault(), true, false), 0);
    node.setInspectionNode(false);
    ((DefaultTreeModel)myTree.getModel()).reload(node);
    myTree.expandPath(new TreePath(node.getPath()));
    myTree.revalidate();
  }

  private List<String> getAvailableScopes(Descriptor descriptor, Project project) {
    final ArrayList<NamedScope> scopes = new ArrayList<NamedScope>();
    for (NamedScopesHolder holder : NamedScopesHolder.getAllNamedScopeHolders(project)) {
      Collections.addAll(scopes, holder.getScopes());
    }
    final Set<NamedScope> used = new HashSet<NamedScope>();
    final List<ScopeToolState> nonDefaultTools = mySelectedProfile.getNonDefaultTools(descriptor.getKey().toString());
    if (nonDefaultTools != null) {
      for (ScopeToolState state : nonDefaultTools) {
        used.add(state.getScope());
      }
    }
    scopes.removeAll(used);

    final List<String> availableScopes = new ArrayList<String>();
    for (NamedScope scope : scopes) {
      availableScopes.add(scope.getName());
    }
    return availableScopes;
  }
}