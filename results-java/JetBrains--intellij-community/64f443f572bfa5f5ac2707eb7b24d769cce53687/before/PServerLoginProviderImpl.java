package com.intellij.cvsSupport2.connections.pserver;

import com.intellij.CvsBundle;
import com.intellij.cvsSupport2.config.CvsApplicationLevelConfiguration;
import com.intellij.cvsSupport2.cvsExecution.ModalityContext;
import com.intellij.cvsSupport2.javacvsImpl.io.ReadWriteStatistics;
import com.intellij.cvsSupport2.javacvsImpl.io.StreamLogger;
import com.intellij.cvsSupport2.util.CvsFileUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.PasswordPromptDialog;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.IConnection;
import org.netbeans.lib.cvsclient.connection.PServerPasswordScrambler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * author: lesya
 */
public class PServerLoginProviderImpl extends PServerLoginProvider {

  private static final Logger LOG = Logger.getInstance("#com.intellij.cvsSupport2.connections.pserver.PServerLoginProviderImpl");

  @Nullable
  public String getScrambledPasswordForCvsRoot(String cvsroot) {
    return getPassword(cvsroot);
  }

  @Nullable
  private static String requestForPassword(String cvsroot) {
    PasswordPromptDialog passwordDialog = new PasswordPromptDialog(CvsBundle.message("propmt.text.enter.password.for.cvs.root", cvsroot),
                                                                   CvsBundle.message("propmt.title.enter.password.for.cvs.root"), null);
    passwordDialog.show();
    if (!passwordDialog.isOK()) return null;
    return PServerPasswordScrambler.getInstance().scramble(passwordDialog.getPassword());
  }

  private static String getMessageFrom(AuthenticationException e) {
    Throwable underlyingThrowable = e.getCause();
    if (underlyingThrowable == null) {
      return e.getLocalizedMessage() == null ? e.getMessage() : e.getLocalizedMessage();
    }

    return underlyingThrowable.getLocalizedMessage() == null ? underlyingThrowable.getMessage()
        : underlyingThrowable.getLocalizedMessage();
  }

  public boolean login(PServerCvsSettings settings, ModalityContext executor, Project project) {
    String cvsRoot = settings.getCvsRootAsString();
    String stored = getPassword(cvsRoot);
    if (stored != null) {
      IConnection connection = settings.createConnection(new ReadWriteStatistics());
      try {
        connection.open(new StreamLogger());
        settings.setOffline(false);
        return true;
      }
      catch (AuthenticationException e) {
        if (settings.checkReportOfflineException(e, project)) {
          return false;
        }
        else {
          settings.showConnectionErrorMessage(getMessageFrom(e), project);
          settings.releasePassword();
          return relogin(settings, executor, project);
        }
      }
      finally {
        try {
          connection.close();
        }
        catch (IOException e) {
          // ignore
        }
      }
    }
    String password = requestForPassword(cvsRoot);
    if (password == null) return false;
    try {
      storePassword(cvsRoot, password);
    }
    catch (IOException e) {
      Messages.showMessageDialog(CvsBundle.message("error.message.cannot.store.password", e.getLocalizedMessage()),
                                 CvsBundle.message("error.title.storing.cvs.password"), Messages.getErrorIcon());
      return false;
    }
    settings.storePassword(password);
    return login(settings, executor, project);
  }

  public boolean relogin(PServerCvsSettings settings, ModalityContext executor, Project project) {
    String cvsRoot = settings.getCvsRootAsString();
    String password = requestForPassword(cvsRoot);
    if (password == null) return false;
    removeAllPasswordsForThisCvsRootFromPasswordFile(cvsRoot);
    try {
      storePassword(cvsRoot, password);
    } catch (IOException e) {
      Messages.showMessageDialog(e.getLocalizedMessage(), CvsBundle.message("error.title.cannot.store.password"), Messages.getErrorIcon());
      return false;
    }
    settings.storePassword(password);
    return login(settings, executor, project);
  }

  private static ArrayList<String> readConfigurationNotMatchedWith(String cvsRoot, File passFile) {
    FileInputStream input;
    try {
      input = new FileInputStream(passFile);
    } catch (FileNotFoundException e) {
      return new ArrayList<String>();
    }

    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    ArrayList<String> result = new ArrayList<String>();
    try {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.indexOf(cvsRoot) == -1) result.add(line);
      }
    } catch (IOException ex) {
      // ignore
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        // ignore
      }
    }
    return result;
  }

  private static void removeAllPasswordsForThisCvsRootFromPasswordFile(String cvsRoot) {
    File passFile = getPassFile();
    if (passFile == null) return;
    if (!passFile.isFile()) return;

    ArrayList<String> lines = readConfigurationNotMatchedWith(cvsRoot, passFile);

    try {
      CvsFileUtil.storeLines(lines, passFile);
    } catch (IOException e) {
      LOG.error(e);
    }
  }

  private static void storePassword(String stringConfiguration, String scrambledPassword) throws IOException {
    File passFile = getPassFile();
    FileUtil.createIfDoesntExist(passFile);
    List<String> lines = CvsFileUtil.readLinesFrom(passFile);
    lines.add(stringConfiguration + " " + scrambledPassword);
    CvsFileUtil.storeLines(lines, passFile);
  }

  @Nullable
  private static String getPassword(String config) {
    File passFile = getPassFile();
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(passFile)));
      try {
        return findPasswordIn(reader, config);
      } finally {
        reader.close();
      }
    } catch (IOException e) {
      return null;
    }
  }

  private static File getPassFile() {
    return new File(CvsApplicationLevelConfiguration.getInstance().getPathToPassFile());
  }

  @Nullable
  private static String findPasswordIn(BufferedReader reader, String config) throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      int position = line.indexOf(config);
      if (position != -1) {
        String result = line.substring(position + config.length());
        return result.substring(1);
      }
    }
    return null;
  }

}