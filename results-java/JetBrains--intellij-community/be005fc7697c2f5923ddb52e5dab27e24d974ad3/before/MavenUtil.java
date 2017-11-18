package org.jetbrains.idea.maven.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.impl.LaterInvocator;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Function;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import gnu.trove.THashSet;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.project.MavenId;
import org.jetbrains.idea.maven.project.MavenProject;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class MavenUtil {
  public static void invokeLater(Project p, Runnable r) {
    invokeInDispatchThread(p, ModalityState.defaultModalityState(), r);
  }

  public static void invokeInDispatchThread(final Project p, final ModalityState state, final Runnable r) {
    if (ApplicationManager.getApplication().isUnitTestMode()
        || ApplicationManager.getApplication().isDispatchThread()) {
      r.run();
    }
    else {
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          if (p.isDisposed()) return;
          r.run();
        }
      }, state);
    }
  }

  public static void invokeAndWait(final Project p, final ModalityState state, final Runnable r) {
    if (ApplicationManager.getApplication().isUnitTestMode()
        || ApplicationManager.getApplication().isDispatchThread()) {
      r.run();
    }
    else {
      ApplicationManager.getApplication().invokeAndWait(new Runnable() {
        public void run() {
          if (p.isDisposed()) return;
          r.run();
        }
      }, state);
    }
  }

  public static void runDumbAware(final Project project, final Runnable r) {
    if (r instanceof DumbAware) {
      r.run();
    }
    else {
      DumbService.getInstance(project).runWhenSmart(new Runnable() {
        public void run() {
          if (project.isDisposed()) return;
          r.run();
        }
      });
    }
  }

  public static void runWhenInitialized(final Project project, final Runnable r) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      r.run();
      return;
    }

    if (!project.isInitialized()) {
      StartupManager.getInstance(project).registerPostStartupActivity(r);
      return;
    }

    runDumbAware(project, r);
  }

  public static boolean isInModalContext() {
    if (ApplicationManager.getApplication().isUnitTestMode()) return false;
    return LaterInvocator.isInModalContext();
  }

  public static File getPluginSystemDir(String folder) {
    // PathManager.getSystemPath() may return relative path
    return new File(PathManager.getSystemPath(), "Maven" + "/" + folder).getAbsoluteFile();
  }

  public static List<String> collectPaths(List<VirtualFile> files) {
    return ContainerUtil.map(files, new Function<VirtualFile, String>() {
      public String fun(VirtualFile file) {
        return file.getPath();
      }
    });
  }

  public static List<VirtualFile> collectFiles(List<MavenProject> projects) {
    return ContainerUtil.map(projects, new Function<MavenProject, VirtualFile>() {
      public VirtualFile fun(MavenProject project) {
        return project.getFile();
      }
    });
  }

  public static <T> boolean equalAsSets(final Collection<T> collection1, final Collection<T> collection2) {
    return toSet(collection1).equals(toSet(collection2));
  }

  private static <T> Collection<T> toSet(final Collection<T> collection) {
    return (collection instanceof Set ? collection : new THashSet<T>(collection));
  }

  public static String formatHtmlImage(URL url) {
    return "<img src=\"" + url + "\"> ";
  }

  public static String makeFileContent(MavenId projectId) {
    return makeFileContent(projectId, false, false);
  }

  public static String makeFileContent(MavenId projectId, boolean inheritGroupId, boolean inheritVersion) {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
           "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
           "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
           "    <modelVersion>4.0.0</modelVersion>\n" +
           (inheritGroupId ? "" : "    <groupId>" + projectId.getGroupId() + "</groupId>\n") +
           "    <artifactId>" +
           projectId.getArtifactId() +
           "</artifactId>\n" +
           (inheritVersion ? "" : "    <version>" + projectId.getVersion() + "</version>\n") +
           "</project>";
  }

  public static <T extends Collection<Pattern>> T collectPattern(String text, T result) {
    String antPattern = FileUtil.convertAntToRegexp(text.trim());
    try {
      result.add(Pattern.compile(antPattern));
    }
    catch (PatternSyntaxException ignore) {
    }
    return result;
  }

  public static boolean isIncluded(String relativeName, List<Pattern> includes, List<Pattern> excludes) {
    boolean result = false;
    for (Pattern each : includes) {
      if (each.matcher(relativeName).matches()) {
        result = true;
        break;
      }
    }
    if (!result) return false;
    for (Pattern each : excludes) {
      if (each.matcher(relativeName).matches()) return false;
    }
    return true;
  }

  public static <T extends Serializable> T cloneObject(T object) {
    try {
      return (T)BeanUtils.cloneBean(object);
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    catch (InstantiationException e) {
      throw new RuntimeException(e);
    }
    catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
    catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  public static void stripDown(Object object) {
    try {
      for (Field each : ReflectionUtil.collectFields(object.getClass())) {
        Class<?> type = each.getType();
        each.setAccessible(true);
        Object value = each.get(object);
        if (value != null
            && (value.getClass().isArray()
                || value instanceof Collection
                || value instanceof Map
                || value instanceof Xpp3Dom)) {
          each.set(object, null);
        }
        else {
          Package pack = type.getPackage();
          if (pack != null && Model.class.getPackage().getName().equals(pack.getName())) {
            if (value != null) stripDown(value);
          }
        }
      }
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public static void run(Project project, String title, final MavenTask task) throws MavenProcessCanceledException {
    final Exception[] canceledEx = new Exception[1];

    ProgressManager.getInstance().run(new Task.Modal(project, title, true) {
      public void run(@NotNull ProgressIndicator i) {
        try {
          task.run(new MavenProgressIndicator(i));
        }
        catch (MavenProcessCanceledException e) {
          canceledEx[0] = e;
        }
        catch (ProcessCanceledException e) {
          canceledEx[0] = e;
        }
      }
    });
    if (canceledEx[0] instanceof MavenProcessCanceledException) throw (MavenProcessCanceledException)canceledEx[0];
    if (canceledEx[0] instanceof ProcessCanceledException) throw new MavenProcessCanceledException();
  }

  public static MavenTaskHandler runInBackground(final Project project,
                                                 final String title,
                                                 final boolean cancellable,
                                                 final MavenTask task) {
    final Semaphore finishSemaphore = new Semaphore();
    final MavenProgressIndicator indicator = new MavenProgressIndicator();

    finishSemaphore.down();

    Runnable runnable = new Runnable() {
      public void run() {
        try {
          task.run(indicator);
        }
        catch (MavenProcessCanceledException ignore) {
          indicator.cancel();
        }
        catch (ProcessCanceledException ignore) {
          indicator.cancel();
        }
        finally {
          finishSemaphore.up();
        }
      }
    };

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      runnable.run();
    }
    else {
      final Future<?> future = ApplicationManager.getApplication().executeOnPooledThread(runnable);
      invokeLater(project, new Runnable() {
        public void run() {
          if (future.isDone()) return;

          new Task.Backgroundable(project, title, cancellable) {
            public void run(@NotNull ProgressIndicator i) {
              indicator.addIndicator(i);
              try {
                future.get();
              }
              catch (InterruptedException e) {
              }
              catch (ExecutionException e) {
              }
            }
          }.queue();
        }
      });
    }

    return new MavenTaskHandler(finishSemaphore, indicator);
  }

  public static class MavenTaskHandler {
    private final Semaphore myFinishSemaphore;
    private final MavenProgressIndicator myIndicator;

    private MavenTaskHandler(Semaphore finishSemaphore, MavenProgressIndicator indicator) {
      myFinishSemaphore = finishSemaphore;
      myIndicator = indicator;
    }

    public void stop() {
      myIndicator.cancel();
    }

    public void waitFor() {
      myFinishSemaphore.waitFor();
    }

    public boolean isCancelled() {
      return myIndicator.isCanceled();
    }
  }
}