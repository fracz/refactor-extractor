package com.intellij.cvsSupport2.connections;

import com.intellij.cvsSupport2.cvsExecution.ModalityContext;
import com.intellij.cvsSupport2.errorHandling.CannotFindCvsRootException;
import com.intellij.cvsSupport2.javacvsImpl.io.ReadWriteStatistics;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.netbeans.lib.cvsclient.CvsRoot;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.connection.IConnection;

import java.io.File;

/**
 * author: lesya
 */
public abstract class CvsRootProvider implements CvsEnvironment{
  private File myLocalRoot;
  private File myAdminRoot;
  protected final CvsEnvironment myCvsEnvironment;

  public static CvsRootProvider createOn(File file) throws CannotFindCvsRootException {
    return CvsRootOnFileSystem.createMeOn(file);
  }

  public static CvsRootProvider createOn(File file, CvsEnvironment env){
    return new CvsRootOnEnvironment(file, env);
  }

  public CvsRootProvider(File rootFile, CvsEnvironment cvsRoot) {
    myLocalRoot = rootFile;
    myAdminRoot = rootFile;
    myCvsEnvironment = cvsRoot;
  }

  public void changeLocalRootTo(@NotNull File localRoot){
    myLocalRoot = localRoot;
  }

  public IConnection createConnection(ReadWriteStatistics statistics) {
    return myCvsEnvironment.createConnection(statistics);
  }

  public boolean login(ModalityContext executor, Project project) {
    return myCvsEnvironment.login(executor, project);
  }

  public CvsRoot getCvsRoot() {
    return myCvsEnvironment.getCvsRoot();
  }

  public String getCvsRootAsString() {
    return myCvsEnvironment.getCvsRootAsString();
  }

  public File getLocalRoot() {
    return myLocalRoot;
  }

  public File getAdminRoot() {
    return myAdminRoot;
  }

  public void changeAdminRootTo(File directory) {
    myAdminRoot = directory;
  }

  public boolean isValid() {
    return myCvsEnvironment.isValid();
  }

  public CommandException processException(CommandException t) {
    return myCvsEnvironment.processException(t);
  }

  public boolean isOffline() {
    return myCvsEnvironment.isOffline();
  }

}