package ru.compscicenter.edide;


import com.intellij.openapi.components.*;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.compscicenter.edide.course.Course;
import ru.compscicenter.edide.course.Lesson;
import ru.compscicenter.edide.course.Task;
import ru.compscicenter.edide.course.TaskFile;

import java.util.HashMap;
import java.util.Map;

/**
 * User: lia
 * Date: 26.12.13
 * Time: 20:37
 */


@State(
  name = "Element",
  storages = {
    @Storage(
      id = "others",
      file = "$PROJECT_CONFIG_DIR$/study_project.xml",
      scheme = StorageScheme.DIRECTORY_BASED
    )}
)
public class StudyTaskManager implements ProjectComponent, PersistentStateComponent<Element> {
  private static Map<String, StudyTaskManager> myTaskManagers = new HashMap<String, StudyTaskManager>();
  private final Project myProject;
  private Course myCourse;

  public void setCourse(Course course) {
    myCourse = course;
  }

  private StudyTaskManager(Project project) {
    myTaskManagers.put(project.getBasePath(), this);
    myProject = project;
    myCourse = null;
  }


  public Course getCourse() {
    return myCourse;
  }

  public Element saveState(String projectName) {
    Element taskManagerElement = new Element(projectName);
    if (myCourse == null) {
      return taskManagerElement;
    }
    for (Lesson lesson:myCourse.getLessons()) {
      taskManagerElement.addContent(lesson.saveState());
    }
    return taskManagerElement;
  }

  @Nullable
  @Override
  public Element getState() {
    Element state = saveState(myProject.getName());
    return state;
  }

  @Override
  public void loadState(Element el) {
    System.out.println("some text");
  }


  @Override
  public void projectOpened() {

  }

  @Override
  public void projectClosed() {

  }

  @Override
  public void initComponent() {
    EditorFactory.getInstance().addEditorFactoryListener(new StudyEditorFactoryListener(), myProject);
  }

  @Override
  public void disposeComponent() {

  }

  @NotNull
  @Override
  public String getComponentName() {
    return "StudyTaskManager";
  }

  public static StudyTaskManager getInstance(Project project) {
    StudyTaskManager item = myTaskManagers.get(project.getBasePath());
    if (item != null) {
      return item;
    }
    StudyTaskManager taskManager = new StudyTaskManager(project);
    return taskManager;
  }

  private int getIndex(String fullName, String logicalName) {
     return Integer.parseInt(fullName.substring(logicalName.length())) - 1;
  }

  public TaskFile getTaskFile(VirtualFile file) {
   String taskDirName = file.getParent().getName();
   String lessonDirName = file.getParent().getParent().getName();
   Task task = myCourse.getLessons().get(getIndex(lessonDirName, "lesson")).getTaskList().get(getIndex(taskDirName, "task"));
   return task.getFile(file.getName());
  }
}