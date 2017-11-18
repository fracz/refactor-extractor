package com.chrisrm.idea.tree;

import com.chrisrm.idea.MTConfig;
import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.FileColorManager;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by eliorb on 09/04/2017.
 */
public class MTProjectViewNodeDecorator implements ProjectViewNodeDecorator {

    private final Map<FileStatus, Color> fileStatusColorMap;

    public MTProjectViewNodeDecorator() {
        fileStatusColorMap = new HashMap<>(18);
        // TODO move into a properties file ?
        fileStatusColorMap.put(FileStatus.NOT_CHANGED_IMMEDIATE, ColorUtil.fromHex("#80CBC4"));
        fileStatusColorMap.put(FileStatus.NOT_CHANGED_RECURSIVE, ColorUtil.fromHex("#80CBC4"));
        fileStatusColorMap.put(FileStatus.DELETED, ColorUtil.fromHex("#F77669"));
        fileStatusColorMap.put(FileStatus.MODIFIED, ColorUtil.fromHex("#80CBC4"));
        fileStatusColorMap.put(FileStatus.ADDED, ColorUtil.fromHex("#C3E887"));
        fileStatusColorMap.put(FileStatus.MERGE, ColorUtil.fromHex("#C792EA"));
        fileStatusColorMap.put(FileStatus.UNKNOWN, ColorUtil.fromHex("#F77669"));
        fileStatusColorMap.put(FileStatus.IGNORED, ColorUtil.fromHex("#515D5D"));
        fileStatusColorMap.put(FileStatus.HIJACKED, ColorUtil.fromHex("#FFCB6B"));
        fileStatusColorMap.put(FileStatus.MERGED_WITH_CONFLICTS, ColorUtil.fromHex("#BC3F3C"));
        fileStatusColorMap.put(FileStatus.MERGED_WITH_BOTH_CONFLICTS, ColorUtil.fromHex("#BC3F3C"));
        fileStatusColorMap.put(FileStatus.MERGED_WITH_PROPERTY_CONFLICTS, ColorUtil.fromHex("#BC3F3C"));
        fileStatusColorMap.put(FileStatus.DELETED_FROM_FS, ColorUtil.fromHex("#626669"));
        fileStatusColorMap.put(FileStatus.SWITCHED, ColorUtil.fromHex("#F77669"));
        fileStatusColorMap.put(FileStatus.OBSOLETE, ColorUtil.fromHex("#FFCB6B"));
        fileStatusColorMap.put(FileStatus.SUPPRESSED, ColorUtil.fromHex("#3C3F41"));

//        MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
//        messageBus.connect()
//                .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
//                    @Override
//                    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
//                        ApplicationManager.getApplication().
//                    }
//
//                    @Override
//                    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
//                    }
//
//                });
    }

    @Override
    public void decorate(PackageDependenciesNode node, ColoredTreeCellRenderer cellRenderer) {

    }

    @Override
    public void decorate(ProjectViewNode node, PresentationData data) {
        VirtualFile file = node.getVirtualFile();
        if (file == null) {
            return;
        }
        Color highlightColor = MTConfig.getInstance().getHighlightColor();
        Project project = node.getProject();

        // Color file status
        colorFileStatus(data, file, project);

        // Fix open/closed icons (TODO USE SETTING FOR THIS)
        setOpenOrClosedIcon(data, file, project);
    }

    /**
     * Try to mimic the "open or closed"  folder feature
     * TODO: listen to tab select changes
     *
     * @param data
     * @param file
     * @param project
     */
    private void setOpenOrClosedIcon(PresentationData data, VirtualFile file, Project project) {
        if (!file.isDirectory()) {
            return;
        }

        final FileEditorManagerEx manager = FileEditorManagerEx.getInstanceEx(project);
        final FileColorManager fileColorManager = FileColorManager.getInstance(project);
        for (EditorWindow editorWindow : manager.getWindows()) {
            VirtualFile[] files = editorWindow.getFiles();
            for (VirtualFile leaf : files) {
                if (leaf.getPath().contains(file.getPath())) {
                    data.setIcon(AllIcons.Nodes.TreeOpen);
                }
            }
        }
    }

    private void colorFileStatus(PresentationData data, VirtualFile file, Project project) {
        FileStatus status = FileStatusManager.getInstance(project).getStatus(file);
        Color colorFromStatus = getColorFromStatus(status);
        if (colorFromStatus != null) {
            data.setForcedTextForeground(colorFromStatus);
        }
    }

    private Color getColorFromStatus(FileStatus status) {
        return fileStatusColorMap.get(status);
    }
}