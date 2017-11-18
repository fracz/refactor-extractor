package com.intellij.codeInspection.ex;

import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.profile.ProfileManager;
import com.intellij.profile.codeInspection.InspectionProfileManager;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: anna
 * Date: Feb 7, 2005
 */
public class EditInspectionToolsSettingsAction implements IntentionAction {
  private final String myShortName;

  public EditInspectionToolsSettingsAction(LocalInspectionTool tool) {
    myShortName = tool.getShortName();
  }

  public EditInspectionToolsSettingsAction(HighlightDisplayKey key) {
    myShortName = key.toString();
  }

  public String getText() {
    return InspectionsBundle.message("edit.options.of.reporter.inspection.text");
  }

  public String getFamilyName() {
    return InspectionsBundle.message("edit.options.of.reporter.inspection.family");
  }

  public boolean isAvailable(Project project, Editor editor, PsiFile file) {
    return true;
  }

  public void invoke(Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    final InspectionProjectProfileManager projectProfileManager = InspectionProjectProfileManager.getInstance(file.getProject());
    final boolean canChooseDifferentProfiles = !projectProfileManager.useProjectLevelProfileSettings();
    final InspectionProfileManager profileManager = InspectionProfileManager.getInstance();
    InspectionProfileImpl inspectionProfile = (InspectionProfileImpl)(canChooseDifferentProfiles ? profileManager.getRootProfile() : projectProfileManager.getProfile((PsiElement)file));
    editToolSettings(project, inspectionProfile, canChooseDifferentProfiles, myShortName, canChooseDifferentProfiles ? profileManager : InspectionProjectProfileManager.getInstance(project));
  }

  public boolean editToolSettings(final Project project, final InspectionProfileImpl inspectionProfile, final boolean canChooseDifferentProfiles){
    return editToolSettings(project, inspectionProfile, canChooseDifferentProfiles, myShortName, InspectionProjectProfileManager.getInstance(project));
  }

  public static boolean editToolSettings(final Project project, final InspectionProfileImpl inspectionProfile, final boolean canChooseDifferentProfile, final String selectedToolShortName, final ProfileManager manager) {
    final boolean isOK = ShowSettingsUtil.getInstance().editConfigurable(project,
                                                                         "#com.intellij.codeInspection.ex.EditInspectionToolsSettingsAction",
                                                                         new InspectionToolsConfigurable(project, canChooseDifferentProfile, manager, inspectionProfile, selectedToolShortName));
    return isOK;
  }

  public boolean startInWriteAction() {
    return false;
  }

  public static boolean filterToolSettings(final Project project,
                                           final InspectionProfileImpl inspectionProfile,
                                           final boolean canChooseDifferentProfile,
                                           @NotNull final String filter,
                                           final ProfileManager profileManager) {
    final InspectionToolsConfigurable inspectionToolsConfigurable =
      new InspectionToolsConfigurable(project, canChooseDifferentProfile, profileManager, inspectionProfile, null){
        public JComponent createComponent() {
          final InspectionToolsPanel toolsPanel = getPanel();
          toolsPanel.filterTree(filter);
          return toolsPanel;
        }

        public void reset() {
          getPanel().setFilter(filter);
        }
      };
    final boolean isOK = ShowSettingsUtil.getInstance()
      .editConfigurable(project, "#com.intellij.codeInspection.ex.EditInspectionToolsSettingsAction", inspectionToolsConfigurable);
    return isOK;
  }

  public static class InspectionToolsConfigurable implements Configurable {
    private boolean myChooseDifferentProfile;
    private InspectionProfileImpl myInspectionProfile;
    private String mySelectedTool;
    private InspectionToolsPanel myPanel;

    public InspectionToolsConfigurable(final Project project,
                                       final boolean chooseDifferentProfile,
                                       final ProfileManager profileManager,
                                       final InspectionProfileImpl inspectionProfile,
                                       final String selectedTool) {
      myChooseDifferentProfile = chooseDifferentProfile;
      myInspectionProfile = inspectionProfile;
      mySelectedTool = selectedTool;
      myPanel =  new InspectionToolsPanel(myInspectionProfile.getName(), project, myChooseDifferentProfile, profileManager);
    }

    public String getDisplayName() {
      final String title = ApplicationBundle.message("title.errors");
      return myChooseDifferentProfile ? title : (title + ": \'" + myInspectionProfile.getName() + "\' inspection profile");
    }

    public Icon getIcon() {
      return IconLoader.getIcon("/general/configurableErrorHighlighting.png");
    }

    public String getHelpTopic() {
      return "preferences.errorHighlight";
    }

    public JComponent createComponent() {
      if (mySelectedTool != null) {
        myPanel.selectInspectionTool(mySelectedTool);
      }
      return myPanel;
    }

    public boolean isModified() {
      return myPanel.isModified();
    }

    public void apply() throws ConfigurationException {
      if (myChooseDifferentProfile) {
        myPanel.apply();
        final InspectionProfileImpl editedProfile = (InspectionProfileImpl)myPanel.getSelectedProfile();
        InspectionProfileManager.getInstance().setRootProfile(editedProfile.getName());
        myInspectionProfile.copyFrom(editedProfile);
      }
      else {
        final InspectionProfileImpl editedProfile = (InspectionProfileImpl)myPanel.getSelectedProfile();
        myInspectionProfile.copyFrom(editedProfile);
        myInspectionProfile.save();
        myPanel.initDescriptors();
      }
    }

    public void reset() {
      myPanel.reset();
    }

    public void disposeUIResources() {
      if (myPanel != null) {
        myPanel.saveVisibleState();
        myPanel = null;
      }
    }

    public InspectionToolsPanel getPanel(){
      return myPanel;
    }
  }
}