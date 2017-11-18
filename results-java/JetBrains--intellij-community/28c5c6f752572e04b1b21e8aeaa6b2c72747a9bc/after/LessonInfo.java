package ru.compscicenter.edide.course;

/**
 * Implementation of class which contains information about student progress in current lesson
 */
public class LessonInfo {
  private int myTaskNum;
  private int myTaskFailed;
  private int myTaskSolved;
  private int myTaskUnchecked;

  public int getTaskNum() {
    return myTaskNum;
  }

  public void setTaskNum(int taskNum) {
    myTaskNum = taskNum;
  }

  public int getTaskFailed() {
    return myTaskFailed;
  }

  public void setTaskFailed(int taskFailed) {
    myTaskFailed = taskFailed;
  }

  public int getTaskSolved() {
    return myTaskSolved;
  }

  public void setTaskSolved(int taskSolved) {
    myTaskSolved = taskSolved;
  }

  public int getTaskUnchecked() {
    return myTaskUnchecked;
  }

  public void setTaskUnchecked(int taskUnchecked) {
    myTaskUnchecked = taskUnchecked;
  }
}