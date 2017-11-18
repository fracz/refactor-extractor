package com.jetbrains.edu.coursecreator.projectView;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.SimpleTextAttributes;
import com.jetbrains.edu.coursecreator.CCProjectService;
import com.jetbrains.edu.coursecreator.format.Course;
import com.jetbrains.edu.coursecreator.format.Lesson;
import com.jetbrains.edu.coursecreator.format.Task;
import org.jetbrains.annotations.NotNull;

public class CCDirectoryNode extends PsiDirectoryNode {
  private final PsiDirectory myValue;
  private final Project myProject;

  public CCDirectoryNode(@NotNull final Project project,
                         PsiDirectory value,
                         ViewSettings viewSettings) {
    super(project, value, viewSettings);
    myValue = value;
    myProject = project;
  }

  @Override
  protected void updateImpl(PresentationData data) {
    //TODO:change presentable name for files with suffix _answer

    String valueName = myValue.getName();
    final Course course = CCProjectService.getInstance(myProject).getCourse();
    if (course == null) return;
    if (myProject.getBaseDir().equals(myValue.getVirtualFile())) {
      data.clearText();
      data.addText(valueName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
      data.addText(" (" + course.getName() + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
      return;
    }
    final Lesson lesson = course.getLesson(valueName);
    if (lesson != null) {
      data.clearText();
      data.addText(valueName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
      data.addText(" (" + lesson.name + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
      return;
    }
    else {
      final PsiDirectory parentDir = myValue.getParentDirectory();
      if (parentDir != null) {
        final Lesson parentLesson = course.getLesson(parentDir.getName());
        if (parentLesson != null) {
          final Task task = parentLesson.getTask(valueName);
          if (task != null) {
            data.clearText();
            data.addText(valueName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            data.addText(" (" + task.name + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
            return;
          }
        }
      }
    }
    data.setPresentableText(valueName);
  }

}