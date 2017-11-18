package com.jetbrains.edu.learning;

import com.intellij.facet.ui.FacetEditorValidator;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectGenerator;
import com.jetbrains.edu.learning.course.CourseInfo;
import com.jetbrains.edu.learning.ui.StudyNewProjectPanel;
import com.jetbrains.python.newProject.PythonProjectGenerator;
import icons.InteractiveLearningPythonIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.Map;


public class PyStudyDirectoryProjectGenerator extends PythonProjectGenerator implements DirectoryProjectGenerator {
  private final StudyProjectGenerator myGenerator;
  public ValidationResult myValidationResult = new ValidationResult("selected course is not valid");

  public PyStudyDirectoryProjectGenerator() {
    myGenerator = new StudyProjectGenerator();
    myGenerator.addSettingsStateListener(new StudyProjectGenerator.SettingsListener() {
      @Override
      public void stateChanged(ValidationResult result) {
        setValidationResult(result);
      }
    });
  }

  @Nls
  @NotNull
  @Override
  public String getName() {
    return "Educational";
  }

  @Nullable
  @Override
  public Object showGenerationSettings(VirtualFile baseDir) throws ProcessCanceledException {
    return null;
  }

  @Nullable
  @Override
  public Icon getLogo() {
    return InteractiveLearningPythonIcons.EducationalProjectType;
  }


  @Override
  public void generateProject(@NotNull final Project project, @NotNull final VirtualFile baseDir,
                              @Nullable Object settings, @NotNull Module module) {
    myGenerator.generateProject(project, baseDir);
  }
  @NotNull
  @Override
  public ValidationResult validate(@NotNull String s) {
    return myValidationResult;
  }

  public void setValidationResult(ValidationResult validationResult) {
    myValidationResult = validationResult;
  }

  @Nullable
  @Override
  public JPanel extendBasePanel() throws ProcessCanceledException {
    StudyNewProjectPanel settingsPanel = new StudyNewProjectPanel(myGenerator);
    settingsPanel.registerValidators(new FacetValidatorsManager() {
      public void registerValidator(FacetEditorValidator validator, JComponent... componentsToWatch) {
        throw new UnsupportedOperationException();
      }
      public void validate() {
        fireStateChanged();
      }
    });
    return settingsPanel.getContentPanel();
  }

  public Map<CourseInfo, File> getCourses() {
    return myGenerator.getCourses();
  }

  public void setSelectedCourse(CourseInfo course) {
    myGenerator.setSelectedCourse(course);
  }
}