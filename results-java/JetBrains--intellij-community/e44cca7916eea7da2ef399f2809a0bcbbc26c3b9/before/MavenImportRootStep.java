package org.jetbrains.idea.maven.project.open;

import com.intellij.ide.util.projectWizard.NamePathComponent;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectImportWizardStep;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.core.MavenCoreSettings;
import org.jetbrains.idea.maven.core.MavenPathsForm;
import org.jetbrains.idea.maven.project.ImporterSettingsForm;
import org.jetbrains.idea.maven.project.MavenImporterSettings;
import org.jetbrains.idea.maven.project.ProjectBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Vladislav.Kaznacheev
 */
class MavenImportRootStep extends ProjectImportWizardStep {

  private MavenCoreSettings myCoreSettings;
  private MavenImportBuilder myImportContext;
  private MavenImporterSettings myImporterSettings;

  private final JPanel myPanel;
  private NamePathComponent myRootPathComponent;
  private final ImporterSettingsForm myImporterSettingsForm;

  public MavenImportRootStep(WizardContext wizardContext) {
    super(wizardContext);

    myImporterSettingsForm = new ImporterSettingsForm(true) {
      public String getDefaultModuleDir() {
        return myRootPathComponent.getPath();
      }
    };

    myPanel = new JPanel(new GridBagLayout());
    myPanel.setBorder(BorderFactory.createEtchedBorder());

    myRootPathComponent = new NamePathComponent("",
                                                ProjectBundle.message("maven.import.label.select.root"),
                                                ProjectBundle.message("maven.import.title.select.root"),
                                                "", false);

    myRootPathComponent.setNameComponentVisible(false);

    myPanel.add(myRootPathComponent, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
                                                            GridBagConstraints.HORIZONTAL, new Insets(5, 6, 0, 6), 0, 0));

    myPanel.add(myImporterSettingsForm.createComponent(), new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                                                                                 GridBagConstraints.HORIZONTAL, new Insets(15, 6, 0, 6),
                                                                                 0, 0));
    JButton advancedButton = new JButton(ProjectBundle.message("maven.advanced.button.name"));
    myPanel.add(advancedButton, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHEAST, 0, new Insets(15, 6, 0, 6),
                                                       0, 0));
    advancedButton.addMouseListener(new MouseAdapter() {
      public void mouseClicked(final MouseEvent event) {
        super.mouseClicked(event);
        ShowSettingsUtil.getInstance().editConfigurable(myPanel, new MavenPathsConfigurable());
      }
    });
  }

  public JComponent getComponent() {
    return myPanel;
  }

  public void updateDataModel() {
    final MavenImporterSettings settings = getImporterSettings();
    myImporterSettingsForm.getData(settings);
    suggestProjectNameAndPath(settings.getDedicatedModuleDir(), myRootPathComponent.getPath());
  }

  public boolean validate() throws ConfigurationException {
    updateDataModel(); // needed to make 'exhaustive search' take an effect.
    return getImportContext().setRootDirectory(myRootPathComponent.getPath());
  }

  public void updateStep() {
    if (!myRootPathComponent.isPathChangedByUser()) {
      final VirtualFile rootDirectory = getImportContext().getRootDirectory();
      final String path;
      if (rootDirectory != null) {
        path = rootDirectory.getPath();
      }
      else {
        path = getWizardContext().getProjectFileDirectory();
      }
      if (path != null) {
        myRootPathComponent.setPath(FileUtil.toSystemDependentName(path));
        myRootPathComponent.getPathComponent().selectAll();
      }
    }
    myImporterSettingsForm.setData(getImporterSettings());
  }

  public JComponent getPreferredFocusedComponent() {
    return myRootPathComponent.getPathComponent();
  }

  private MavenCoreSettings getCoreSettings() {
    if (myCoreSettings == null) {
      myCoreSettings = ((MavenImportBuilder)getBuilder()).getCoreState();
    }
    return myCoreSettings;
  }

  public MavenImportBuilder getImportContext() {
    if (myImportContext == null) {
      myImportContext = (MavenImportBuilder)getBuilder();
    }
    return myImportContext;
  }

  public MavenImporterSettings getImporterSettings() {
    if (myImporterSettings == null) {
      myImporterSettings = ((MavenImportBuilder)getBuilder()).getImporterPreferences();
    }
    return myImporterSettings;
  }

  @NonNls
  public String getHelpId() {
    return "reference.dialogs.new.project.import.maven.page1";
  }

  class MavenPathsConfigurable implements Configurable {
    MavenPathsForm myForm = new MavenPathsForm();

    @Nls
    public String getDisplayName() {
      return ProjectBundle.message("maven.paths.configurable.name");
    }

    @Nullable
    public Icon getIcon() {
      return null;
    }

    @Nullable
    @NonNls
    public String getHelpTopic() {
      return null;
    }

    public JComponent createComponent() {
      return myForm.getPanel();
    }

    public boolean isModified() {
      return myForm.isModified(getCoreSettings());
    }

    public void apply() throws ConfigurationException {
      myForm.setData(getCoreSettings());
    }

    public void reset() {
      myForm.getData(getCoreSettings());
    }

    public void disposeUIResources() {
      myForm.disposeUIResources();
    }
  }
}