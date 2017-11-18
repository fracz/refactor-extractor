package org.jetbrains.idea.maven.project;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.Semaphore;
import org.jetbrains.annotations.NotNull;

public class MavenProcess {
  private final ProgressIndicator myIndicator;

  public MavenProcess(ProgressIndicator i) {
    myIndicator = i;
  }

  public ProgressIndicator getIndicator() {
    return myIndicator;
  }

  public void setText(String s) {
    if (myIndicator != null) myIndicator.setText(s);
  }

  public void setText2(String s) {
    if (myIndicator != null) myIndicator.setText2(s);
  }

  public void setFraction(double value) {
    if (myIndicator != null) myIndicator.setFraction(value);
  }

  public boolean isCanceled() {
    return myIndicator != null && myIndicator.isCanceled();
  }

  public void checkCanceled() throws MavenProcessCanceledException {
    if (isCanceled()) throw new MavenProcessCanceledException();
  }

  public static void run(Project project, String title, final MavenTask task) throws MavenProcessCanceledException {
    final MavenProcessCanceledException[] canceledEx = new MavenProcessCanceledException[1];

    ProgressManager.getInstance().run(new Task.Modal(project, title, true) {
      public void run(@NotNull ProgressIndicator i) {
        try {
          task.run(new MavenProcess(i));
        }
        catch (MavenProcessCanceledException e) {
          canceledEx[0] = e;
        }
      }
    });
    if (canceledEx[0] != null) throw canceledEx[0];
  }

  public static MavenTaskHandler runInBackground(final Project project,
                                                 final String title,
                                                 final boolean canBeCancelled,
                                                 MavenTask task) {
    final Semaphore startSemaphore = new Semaphore();
    final Semaphore finishSemaphore = new Semaphore();
    final ProgressIndicator[] indicator = new ProgressIndicator[1];

    startSemaphore.down();
    finishSemaphore.down();

    final MavenTask[] taskHolder = new MavenTask[]{task};

    ProgressManager.getInstance().run(new Task.Backgroundable(project, title, canBeCancelled) {
      public void run(@NotNull ProgressIndicator i) {
        try {
          indicator[0] = i;
          startSemaphore.up();
          taskHolder[0].run(new MavenProcess(i));
        }
        catch (MavenProcessCanceledException ignore) {
        }
        finally {
          finishSemaphore.up();
          taskHolder[0] = null; // memory leaks prevention
        }
      }
    });

    return new MavenTaskHandler(startSemaphore, finishSemaphore, indicator);
  }

  public static class MavenTaskHandler {
    private final Semaphore myStartSemaphore;
    private final Semaphore myFinishSemaphore;
    private final ProgressIndicator[] myIndicator;

    private MavenTaskHandler(Semaphore startSemaphore,
                             Semaphore finishSemaphore,
                             ProgressIndicator[] indicator) {
      myStartSemaphore = startSemaphore;
      myFinishSemaphore = finishSemaphore;
      myIndicator = indicator;
    }

    public void stop() {
      myStartSemaphore.waitFor();
      myIndicator[0].cancel();
    }

    public void waitFor() {
      myFinishSemaphore.waitFor();
    }

    public boolean waitFor(long timeout) {
      return myFinishSemaphore.waitFor(timeout);
    }

    public void stopAndWaitForFinish() {
      stop();
      waitFor();
    }
  }

  public static abstract class MavenTask {
    public abstract void run(MavenProcess process) throws MavenProcessCanceledException;
  }
}