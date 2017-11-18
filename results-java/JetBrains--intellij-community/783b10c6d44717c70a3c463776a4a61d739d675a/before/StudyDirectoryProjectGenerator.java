package ru.compscicenter.edide;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.intellij.facet.ui.ValidationResult;
import com.intellij.lang.javascript.boilerplate.GithubDownloadUtil;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.platform.DirectoryProjectGenerator;
import com.intellij.platform.templates.github.ZipUtil;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.compscicenter.edide.course.Course;

import java.io.File;
import java.io.IOException;
import java.io.*;
import java.util.*;

/**
 * User: lia
 */
class StudyDirectoryProjectGenerator implements DirectoryProjectGenerator {
  private static final Logger LOG = Logger.getInstance(StudyDirectoryProjectGenerator.class.getName());
  public static final String REPO_URL = "https://github.com/medvector/initial-python-course/archive/master.zip";
  public static final String USER_NAME = "medvector";
  public static final String REPOSITORY_NAME = "initial-python-course";
  private static File myBaseCourseFile;
  private static File myDefaultCoursesBaseDir;
  private static Map<String, File> myDefaultCourseFiles =  new HashMap<String, File>();
  private Map<String, File> myDefaultCourses;
  private static String myLocalCourseBaseFileName;
  private static String myDefaultSelectedCourseName;

  @Nls
  @NotNull
  @Override
  public String getName() {
    return "Study project";
  }


  public static void setMyLocalCourseBaseFileName(String fileName) {
    myLocalCourseBaseFileName = fileName;
  }

  public static void setMyDefaultSelectedCourseName(String defaultSelectedCourseName) {
    myDefaultSelectedCourseName = defaultSelectedCourseName;
  }

  public StudyDirectoryProjectGenerator() {
    myDefaultCoursesBaseDir = new File(PathManager.getLibPath() + "/courses");
  }


  public File getBaseCourseFile() {
    if (myLocalCourseBaseFileName != null) {
      File file = new File(myLocalCourseBaseFileName);
      if (file.exists()) {
        return file;
      } else {
        LOG.error("such course file doesn't exist");
      }
    } else {
      if (myDefaultSelectedCourseName != null) {
        File file = myDefaultCourses.get(myDefaultSelectedCourseName);
        if (file!=null && file.exists()) {
          return file;
        }
      } else {
        if (myDefaultCourses.size() > 0) {
          return myDefaultCourses.entrySet().iterator().next().getValue();
        }
      }
    }
    return null;
  }


  public static File getResourcesRoot() {
    @NonNls String jarPath = PathUtil.getJarPathForClass(StudyDirectoryProjectGenerator.class);
    if (jarPath.endsWith(".jar")) {
      final File jarFile = new File(jarPath);
      return jarFile.getParentFile().getParentFile();
    }

    return new File(jarPath);
  }


  public static void setMyBaseCourseFile(String mymyBaseCouseFile) {
    myBaseCourseFile = myDefaultCourseFiles.get(mymyBaseCouseFile);
  }

  @Nullable
  @Override
  public Object showGenerationSettings(VirtualFile baseDir) throws ProcessCanceledException {
    return null;
  }

  //should be invoked in invokeLater method
  void createFile(@NotNull final String name, @NotNull final VirtualFile directory) throws IOException {
    final File root = getResourcesRoot();
    String systemIndependentName = FileUtil.toSystemIndependentName(name);
    final int index = systemIndependentName.lastIndexOf("/");
    if (index > 0) {
      systemIndependentName = systemIndependentName.substring(index + 1);
    }
    FileUtil.copy(new File(root, name), new File(directory.getPath(), systemIndependentName));
  }

  public static ArrayList<String> getCourseFiles() {
    ArrayList<String> fileName = new ArrayList<String>();
    for (String key : myDefaultCourseFiles.keySet()) {
     fileName.add(key);
    }
    return fileName;
  }

  @Override
  public void generateProject(@NotNull final Project project, @NotNull final VirtualFile baseDir,
                              @Nullable Object settings, @NotNull Module module) {
    if (!myDefaultCoursesBaseDir.exists()) {
      downloadCoursesFromGithub();
    }
    myDefaultCourses = getDefaultCourses();
    ////select course window
    StudyNewCourseDialog dlg = new StudyNewCourseDialog(project, myDefaultCourses.keySet());
    dlg.show();

    myBaseCourseFile = getBaseCourseFile();
    if (myBaseCourseFile == null) {
      LOG.error("user didn't choose any course files");
      return;
    }
    try {
      Reader reader = new InputStreamReader(new FileInputStream(myBaseCourseFile));
      Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
      Course course = gson.fromJson(reader, Course.class);
      course.create(project, baseDir, myBaseCourseFile.getParent());
      VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    //System.out.println();
    //
    //ApplicationManager.getApplication().invokeLater(
    //  new Runnable() {
    //    @Override
    //    public void run() {
    //      ApplicationManager.getApplication().runWriteAction(new Runnable() {
    //        @Override
    //        public void run() {
    //          try {
    //            //StudyPlugin.createTaskManager(project.getName());
    //            //TaskManager taskManager = TaskManager.getInstance(project);
    //            TaskManager taskManager = TaskManager.getInstance(project);
    //            int tasksNumber = taskManager.getTasksNum();
    //            for (int task = 0; task < tasksNumber; task++) {
    //              VirtualFile taskDirectory = baseDir.createChildDirectory(this, "task" + (task + 1));
    //              for (int file = 0; file < taskManager.getTaskFileNum(task); file++) {
    //                final String curFileName = taskManager.getFileName(task, file);
    //                createFile(curFileName, taskDirectory);
    //              }
    //              final VirtualFile ideaDir = baseDir.findChild(".idea");
    //              if (ideaDir != null) {
    //                createFile(taskManager.getTest(task), ideaDir);
    //              }
    //              else {
    //                LOG.error("Could not find .idea directory");
    //              }
    //            }
    //          }
    //          catch (IOException e) {
    //            Log.print("Problems with creating files");
    //            Log.print(e.toString());
    //            Log.flush();
    //          }
    //          LocalFileSystem.getInstance().refresh(false);
    //        }
    //      });
    //    }
    //  }
    //);
  }

  private static boolean downloadCoursesFromGithub() {
    File outputFile = new File(PathManager.getLibPath() + "/courses.zip");
      try {
        GithubDownloadUtil.downloadAtomically(null, REPO_URL,
                                              outputFile, USER_NAME, REPOSITORY_NAME);
        if (outputFile.exists()) {
          ZipUtil.unzip(null, myDefaultCoursesBaseDir, outputFile, null, null, true);
          if (myDefaultCoursesBaseDir.exists()) {
            return true;
          }
        }
        return false;
      }
      catch (IOException e) {
        e.printStackTrace();
        return false;
      }
  }

  private  static Map<String, File> getDefaultCourses() {
    Map<String, File> defaultCourses = new HashMap<String, File>();
    if (!myDefaultCoursesBaseDir.exists()) {
      return defaultCourses;
    }
    try {
      File[] files = myDefaultCoursesBaseDir.listFiles();
      if (files != null) {
        for (File f:files) {
          if (f.isDirectory()) {
            File[] filesInCourse = f.listFiles();
            if (filesInCourse != null) {
              for (File courseFile:filesInCourse) {
                if (courseFile.getName().equals("course.json")) {
                  String name = getCourseName(courseFile);
                  int i = 2;
                  if (name!= null) {
                    File item = defaultCourses.get(name);
                    String tmp = name;
                    while(item!= null && !FileUtil.filesEqual(item, courseFile)) {
                      if (i>2)  {
                        name = name.substring(0, name.length() - 2);
                      }
                      name = name + Integer.toString(i);
                      i++;
                      item = defaultCourses.get(name);
                    }
                    defaultCourses.put(name, courseFile);
                  }
                }
              }
            }
          }
        }
      }

    }
    catch (NullPointerException e) {
      LOG.error("default course folder doesn't exist");
    }
    return defaultCourses;
  }

  private static String getCourseName(File file) {
    InputStream metaIS = null;
    String name = null;
    try {
      metaIS = new FileInputStream(file);
      BufferedReader reader = new BufferedReader(new InputStreamReader(metaIS));
      com.google.gson.stream.JsonReader r = new com.google.gson.stream.JsonReader(reader);
      JsonParser parser = new JsonParser();
      com.google.gson.JsonElement el = parser.parse(r);
      name  = el.getAsJsonObject().get("name").getAsString();
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return name;
  }

  @NotNull
  @Override
  public ValidationResult validate(@NotNull String s) {
    return ValidationResult.OK;
  }

  public static Set<String> updateDefaultCourseList() {
    downloadCoursesFromGithub();
    if (myDefaultCoursesBaseDir.exists()) {
      return getDefaultCourses().keySet();
    }
    return new HashSet<String>();
  }
}