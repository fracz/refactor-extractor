/*
 * Copyright 2015 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.classyshark.ui.tabs.viewerpanel.tree;

import com.google.classyshark.ui.tabs.TabsFrame;
import com.google.classyshark.ui.tabs.viewerpanel.ViewerPanel;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.util.List;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

public class FilesTree {
    private final ViewerPanel viewerPanel   ;
    private DefaultTreeModel treeModel = null;
    private JTree jTree = null;

    public FilesTree(ViewerPanel viewerPanel) {
        treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
        jTree = new JTree(treeModel);
        configureJTree(jTree);
        this.viewerPanel = viewerPanel;
    }

    public void fillArchive(File loadedFile, List<String> displayedClassNames) {
        TreeNode rootNode;
        if (loadedFile.getName().endsWith("dex") ||
                loadedFile.getName().endsWith("apk")) {
            rootNode = createJTreeModelAndroid(loadedFile.getName(), displayedClassNames);
        } else {
            rootNode = createJTreeModelClass(loadedFile.getName(), displayedClassNames);
        }

        treeModel.setRoot(rootNode);
    }

    private TreeNode createJTreeModelAndroid(String fileName, List<String> displayedClassNames) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(fileName);
        DefaultMutableTreeNode classes = new DefaultMutableTreeNode("classes");

        String lastPackage = null;
        DefaultMutableTreeNode packageNode = null;
        for (int i = 0; i < displayedClassNames.size(); i++) {
            String resName = displayedClassNames.get(i);
            if (resName.equals("AndroidManifest.xml")) {
                root.add(new DefaultMutableTreeNode(resName));
            } else {
                String pkg = resName.substring(0, resName.lastIndexOf('.'));
                if (lastPackage == null || !pkg.equals(lastPackage)) {
                    if (packageNode != null) {
                        classes.add(packageNode);
                    }
                    lastPackage = pkg;
                    packageNode = new DefaultMutableTreeNode(pkg);
                }
                packageNode.add(new DefaultMutableTreeNode(resName));
            }
        }
        root.add(classes);
        return root;
    }

    private TreeNode createJTreeModelClass(String fileName, List<String> displayedClassNames) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(fileName);
        DefaultMutableTreeNode classes = new DefaultMutableTreeNode("classes");
        String lastPackage = null;
        DefaultMutableTreeNode packageNode = null;

        for (int i = 0; i < displayedClassNames.size(); i++) {
            String fullClassName = displayedClassNames.get(i);
            String pkg = fullClassName.substring(0, fullClassName.lastIndexOf('.'));
            pkg = pkg.substring(0, pkg.lastIndexOf('.'));

            if (lastPackage == null || !pkg.equals(lastPackage)) {
                lastPackage = pkg;
                packageNode = new DefaultMutableTreeNode(pkg);
                classes.add(packageNode);
            }
            packageNode.add(new DefaultMutableTreeNode(fullClassName));

        }
        root.add(classes);
        return root;
    }


    public Component getJTree() {
        return jTree;
    }

    private void configureJTree(final JTree jTree) {
        jTree.setRootVisible(false);
        jTree.setBackground(TabsFrame.ColorScheme.BACKGROUND);
        DefaultTreeCellRenderer cellRenderer = (DefaultTreeCellRenderer) jTree.getCellRenderer();
        cellRenderer.setBackground(TabsFrame.ColorScheme.BACKGROUND);
        cellRenderer.setBackgroundNonSelectionColor(TabsFrame.ColorScheme.BACKGROUND);
        cellRenderer.setTextNonSelectionColor(TabsFrame.ColorScheme.FOREGROUND_CYAN);
        cellRenderer.setFont(new Font("Menlo", Font.PLAIN, 18));
        jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                Object selection = jTree.getLastSelectedPathComponent();
                if (selection == null) return;

                DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) selection;
                if (!defaultMutableTreeNode.isLeaf()) return;

                FilesTree.this.viewerPanel.onSelectedClassName((String) defaultMutableTreeNode.getUserObject());
            }
        });
    }

    public void setVisibleRoot() {
        jTree.setRootVisible(true);
    }
}