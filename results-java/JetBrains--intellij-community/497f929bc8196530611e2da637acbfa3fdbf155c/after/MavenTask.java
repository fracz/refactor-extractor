package org.jetbrains.idea.maven.utils;

public interface MavenTask {
  void run(MavenProgressIndicator process) throws MavenProcessCanceledException;
}