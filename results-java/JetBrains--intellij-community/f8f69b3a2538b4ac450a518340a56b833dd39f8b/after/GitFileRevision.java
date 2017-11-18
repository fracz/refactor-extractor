package git4idea;
/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 *
 * Copyright 2007 Decentrix Inc
 * Copyright 2007 Aspiro AS
 * Copyright 2008 MQSoftware
 * Authors: gevession, Erlend Simonsen & Mark Scott
 *
 * This code was originally derived from the MKS & Mercurial IDEA VCS plugins
 */

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.GitSimpleHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * Git file revision
 */
public class GitFileRevision implements VcsFileRevision, Comparable<VcsFileRevision> {
  /**
   * encoding to be used for binary output
   */
  final static Charset BIN_ENCODING = Charset.forName("ISO-8859-1");
  private final FilePath path;
  private final GitRevisionNumber revision;
  private final String author;
  private final String message;
  private byte[] content;
  private final Project project;
  private final String branch;

  public GitFileRevision(@NotNull Project project,
                         @NotNull FilePath path,
                         @NotNull GitRevisionNumber revision,
                         @Nullable String author,
                         @Nullable String message,
                         @Nullable String branch) {
    this.project = project;
    this.path = path;
    this.revision = revision;
    this.author = author;
    this.message = message;
    this.branch = branch;
  }

  public VcsRevisionNumber getRevisionNumber() {
    return revision;
  }

  public Date getRevisionDate() {
    return revision.getTimestamp();
  }

  public String getAuthor() {
    return author;
  }

  public String getCommitMessage() {
    return message;
  }

  public String getBranchName() {
    return branch;
  }

  public synchronized void loadContent() throws VcsException {
    final VirtualFile root = GitUtil.getGitRoot(project, path);
    GitSimpleHandler h = new GitSimpleHandler(project, root, "show");
    h.setNoSSH(true);
    h.setCharset(BIN_ENCODING);
    h.addParameters(revision.getRev() + ":" + GitUtil.relativePath(root, path));
    String result = h.run();
    try {
      content = result.getBytes(BIN_ENCODING.name());
    }
    catch (UnsupportedEncodingException e) {
      throw new VcsException("Unable to locate encoding: " + BIN_ENCODING.name() + " Reason: " + e.toString(), e);
    }
  }

  public synchronized byte[] getContent() throws IOException {
    if (content == null) {
      try {
        loadContent();
      }
      catch (VcsException e) {
        throw new IOException(e.getMessage());
      }
    }
    return content;
  }

  public int compareTo(VcsFileRevision rev) {
    if (rev instanceof GitFileRevision) return revision.compareTo(((GitFileRevision)rev).revision);
    return getRevisionDate().compareTo(rev.getRevisionDate());
  }
}