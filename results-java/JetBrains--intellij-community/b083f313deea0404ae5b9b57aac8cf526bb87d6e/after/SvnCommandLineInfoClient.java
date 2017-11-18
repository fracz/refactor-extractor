/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.svn.commandLine;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import org.jetbrains.idea.svn.SvnApplicationSettings;
import org.jetbrains.idea.svn.SvnBindUtil;
import org.jetbrains.idea.svn.SvnCommitRunner;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.checkin.IdeaSvnkitBasedAuthenticationCallback;
import org.jetbrains.idea.svn.config.SvnBindException;
import org.jetbrains.idea.svn.portable.SvnExceptionWrapper;
import org.jetbrains.idea.svn.portable.SvnkitSvnWcClient;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.wc.ISVNInfoHandler;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 1/27/12
 * Time: 12:59 PM
 */
public class SvnCommandLineInfoClient extends SvnkitSvnWcClient {

  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.svn.commandLine.SvnCommandLineInfoClient");
  private final Project myProject;

  public SvnCommandLineInfoClient(final Project project) {
    // TODO: Remove svn kit client instantiation
    super(SvnVcs.getInstance(project).createWCClient());
    myProject = project;
  }

  @Override
  public void doInfo(File path, SVNRevision revision, boolean recursive, ISVNInfoHandler handler) throws SVNException {
    doInfo(path, SVNRevision.UNDEFINED, revision, recursive ? SVNDepth.INFINITY : SVNDepth.EMPTY, null, handler);
  }

  @Override
  public void doInfo(File path, SVNRevision pegRevision, SVNRevision revision, boolean recursive, ISVNInfoHandler handler)
    throws SVNException {
    doInfo(path, pegRevision, revision, recursive ? SVNDepth.INFINITY : SVNDepth.EMPTY, null, handler);
  }

  @Override
  public void doInfo(File path,
                     SVNRevision pegRevision,
                     SVNRevision revision,
                     SVNDepth depth,
                     Collection changeLists,
                     final ISVNInfoHandler handler) throws SVNException {
    File base = path.isDirectory() ? path : path.getParentFile();
    base = SvnBindUtil.correctUpToExistingParent(base);
    if (base == null) {
      // very unrealistic
      throw new SVNException(SVNErrorMessage.create(SVNErrorCode.IO_ERROR), new RuntimeException("Can not find existing parent file"));
    }
    issueCommand(path.getPath(), pegRevision, revision, depth, changeLists, handler, base);
  }

  private void issueCommand(String path, SVNRevision pegRevision,
                            SVNRevision revision,
                            SVNDepth depth,
                            Collection changeLists,
                            final ISVNInfoHandler handler, File base) throws SVNException {
    final SvnSimpleCommand command = SvnCommandFactory.createSimpleCommand(myProject, base, SvnCommandName.info);
    List<String> parameters = new ArrayList<String>();

    fillParameters(path, pegRevision, revision, depth, parameters);
    command.addParameters(parameters);
    SvnCommandLineStatusClient.changelistsToCommand(changeLists, command);

    parseResult(handler, base, execute(command));
  }

  private String execute(SvnSimpleCommand command) throws SVNException {
    try {
      return command.run();
    }
    catch (VcsException e) {
      final String text = e.getMessage();
      final boolean notEmpty = !StringUtil.isEmptyOrSpaces(text);
      if (notEmpty && text.contains("W155010")) {
        // just null
        return null;
      }
      // not a working copy exception
      // "E155007: '' is not a working copy"
      if (notEmpty && text.contains("is not a working copy")) {
        throw new SVNException(SVNErrorMessage.create(SVNErrorCode.WC_NOT_WORKING_COPY), e);
      }
      throw new SVNException(SVNErrorMessage.create(SVNErrorCode.IO_ERROR), e);
    }
  }

  private void parseResult(final ISVNInfoHandler handler, File base, String result) throws SVNException {
    if (StringUtil.isEmpty(result)) {
      return;
    }

    final SvnInfoHandler[] infoHandler = new SvnInfoHandler[1];
    infoHandler[0] = new SvnInfoHandler(base, new Consumer<SVNInfo>() {
      @Override
      public void consume(SVNInfo info) {
        try {
          handler.handleInfo(info);
        }
        catch (SVNException e) {
          throw new SvnExceptionWrapper(e);
        }
      }
    });

    try {
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

      parser.parse(new ByteArrayInputStream(result.getBytes(CharsetToolkit.UTF8_CHARSET)), infoHandler[0]);
    }
    catch (SvnExceptionWrapper e) {
      throw (SVNException) e.getCause();
    } catch (IOException e) {
      throw new SVNException(SVNErrorMessage.create(SVNErrorCode.IO_ERROR), e);
    }
    catch (ParserConfigurationException e) {
      throw new SVNException(SVNErrorMessage.create(SVNErrorCode.IO_ERROR), e);
    }
    catch (SAXException e) {
      throw new SVNException(SVNErrorMessage.create(SVNErrorCode.IO_ERROR), e);
    }
  }

  private void fillParameters(String path, SVNRevision pegRevision, SVNRevision revision, SVNDepth depth, Collection<String> parameters) {
    if (depth != null) {
      parameters.add("--depth");
      parameters.add(depth.getName());
    }
    if (revision != null && ! SVNRevision.UNDEFINED.equals(revision) && ! SVNRevision.WORKING.equals(revision)) {
      parameters.add("-r");
      parameters.add(revision.toString());
    }
    parameters.add("--xml");
    if (pegRevision != null && ! SVNRevision.UNDEFINED.equals(pegRevision) && ! SVNRevision.WORKING.equals(pegRevision)) {
      parameters.add(path + "@" + pegRevision.toString());
    } else {
      parameters.add(path);
    }
  }

  @Override
  public void doInfo(SVNURL url, SVNRevision pegRevision, SVNRevision revision, boolean recursive, ISVNInfoHandler handler)
    throws SVNException {
    doInfo(url, pegRevision, revision, recursive ? SVNDepth.INFINITY : SVNDepth.EMPTY, handler);
  }

  @Override
  public void doInfo(SVNURL url, SVNRevision pegRevision, SVNRevision revision, SVNDepth depth, ISVNInfoHandler handler)
    throws SVNException {
    String path = url.toDecodedString();
    List<String> parameters = new ArrayList<String>();

    fillParameters(path, pegRevision, revision, depth, parameters);
    File base = new File(myProject.getBasePath());
    String result = CommandUtil.runSimple(SvnCommandName.info, SvnVcs.getInstance(myProject), url, parameters).getOutput();

    parseResult(handler, base, result);
  }

  @Override
  public SVNInfo doInfo(File path, SVNRevision revision) throws SVNException {
    final SVNInfo[] infoArr = new SVNInfo[1];
    doInfo(path, SVNRevision.UNDEFINED, revision, SVNDepth.EMPTY, null, new ISVNInfoHandler() {
      @Override
      public void handleInfo(SVNInfo info) throws SVNException {
        infoArr[0] = info;
      }
    });
    return infoArr[0];
  }

  @Override
  public SVNInfo doInfo(SVNURL url, SVNRevision pegRevision, SVNRevision revision) throws SVNException {
    final SVNInfo[] infoArr = new SVNInfo[1];
    doInfo(url, pegRevision, revision, SVNDepth.EMPTY, new ISVNInfoHandler() {
      @Override
      public void handleInfo(SVNInfo info) throws SVNException {
        infoArr[0] = info;
      }
    });
    return infoArr[0];
  }
}