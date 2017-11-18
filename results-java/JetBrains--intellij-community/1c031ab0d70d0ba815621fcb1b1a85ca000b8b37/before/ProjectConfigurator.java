package org.jetbrains.idea.maven.project;

import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectEx;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import org.apache.maven.project.MavenProject;
import org.jetbrains.idea.maven.core.util.IdeaAPIHelper;
import org.jetbrains.idea.maven.state.MavenProjectsManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.io.IOException;


public class ProjectConfigurator {
  private Project myProject;
  private ModifiableModuleModel myModuleModel;
  private MavenProjectModel myProjectModel;
  private MavenToIdeaMapping myMapping;
  private Collection<String> myProfiles;
  private MavenImporterSettings mySettings;

  public static void config(Project p,
                            MavenProjectModel projectModel,
                            Collection<String> profiles,
                            MavenToIdeaMapping mapping,
                            MavenImporterSettings settings) {
    ProjectConfigurator c = new ProjectConfigurator(p, projectModel, mapping, profiles, settings);
    c.config();
  }

  private ProjectConfigurator(Project p,
                              MavenProjectModel projectModel,
                              MavenToIdeaMapping mapping,
                              Collection<String> profiles,
                              MavenImporterSettings settings) {
    myProject = p;
    myProjectModel = projectModel;
    myMapping = mapping;
    myProfiles = profiles;
    mySettings = settings;
  }

  private void config() {
    deleteObsoleteModules();

    myModuleModel = ModuleManager.getInstance(myProject).getModifiableModel();
    configSettings();
    configModules();
    configModuleGroups();
    resolveDependenciesAndCommit();
  }

  private void deleteObsoleteModules() {
    List<Module> obsolete = new ArrayList<Module>();

    for (Module m : myMapping.getObsoleteModules()) {
      if (MavenProjectsManager.getInstance(myProject).isImportedModule(m)) {
        obsolete.add(m);
      }
    }

    if (obsolete.isEmpty()) return;

    MavenProjectsManager.getInstance(myProject).setUserModules(obsolete);

    String formatted = StringUtil.join(obsolete, new Function<Module, String>() {
      public String fun(Module m) {
        return "'" + m.getName() + "'";
      }
    }, "\n");

    int result = Messages.showYesNoDialog(myProject,
                                          ProjectBundle.message("maven.import.message.delete.obsolete", formatted),
                                          ProjectBundle.message("maven.import"),
                                          Messages.getQuestionIcon());
    if (result == 1) return;// NO

    IdeaAPIHelper.deleteModules(myMapping.getObsoleteModules());
  }

  private void configSettings() {
    ((ProjectEx)myProject).setSavePathsRelative(true);
  }

  private void configModules() {
    final List<Module> modules = new ArrayList<Module>();

    myProjectModel.visit(new MavenProjectModel.MavenProjectVisitorRoot() {
      public void visit(MavenProjectModel.Node node) {
        modules.add(configModule(node));
        for (MavenProjectModel.Node child : node.mySubProjectsTopoSorted) {
          modules.add(configModule(child));
        }
      }
    });

    MavenProjectsManager.getInstance(myProject).setImportedModules(modules);
  }

  private Module configModule(MavenProjectModel.Node node) {
    MavenProject mavenProject = node.getMavenProject();

    Module module = myMapping.getModule(node);
    if (module == null) {
      String path = myMapping.getModuleFilePath(node);
      // for some reason newModule opens the existing iml file, so we
      // have to remove it beforehand.
      removeExistingIml(path);
      module = myModuleModel.newModule(path, StdModuleTypes.JAVA);
    }

    new ModuleConfigurator(myModuleModel, myMapping, myProfiles, mySettings, module, mavenProject).config();
    return module;
  }

  private void removeExistingIml(String path)  {
    VirtualFile existingFile = LocalFileSystem.getInstance().findFileByPath(path);
    if (existingFile == null) return;
    try {
      existingFile.delete(this);
    }
    catch (IOException ignore) {
    }
  }

  private void configModuleGroups() {
    if (!mySettings.isCreateModuleGroups()) return;

    final Stack<String> groups = new Stack<String>();

    final boolean createTopLevelGroup = myProjectModel.getRootProjects().size() > 1;

    myProjectModel.visit(new MavenProjectModel.MavenProjectVisitorPlain() {
      int depth = 0;

      public void visit(MavenProjectModel.Node node) {
        depth++;

        String name = myMapping.getModuleName(node.getProjectId());

        if (shouldCreateGroup(node)) {
          groups.push(ProjectBundle.message("module.group.name", name));
        }

        Module module = myModuleModel.findModuleByName(name);
        myModuleModel.setModuleGroupPath(module, groups.isEmpty() ? null : groups.toArray(new String[groups.size()]));
      }

      public void leave(MavenProjectModel.Node node) {
        if (shouldCreateGroup(node)) {
          groups.pop();
        }
        depth--;
      }

      private boolean shouldCreateGroup(MavenProjectModel.Node node) {
        return !node.mySubProjects.isEmpty() && (createTopLevelGroup || depth > 1);
      }
    });
  }

  private void resolveDependenciesAndCommit() {
    for (Module module : myMapping.getExistingModules()) {
      RootModelAdapter a = new RootModelAdapter(module);
      a.resolveModuleDependencies(myMapping.getLibraryNameToModuleName());
    }
    myModuleModel.commit();
  }
}