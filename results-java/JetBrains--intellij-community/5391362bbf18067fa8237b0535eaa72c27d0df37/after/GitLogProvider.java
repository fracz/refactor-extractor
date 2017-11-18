/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package git4idea.log;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsKey;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.vcs.log.*;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.GitVcs;
import git4idea.commands.GitCommand;
import git4idea.commands.GitSimpleHandler;
import git4idea.history.GitHistoryUtils;
import git4idea.history.GitLogParser;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Kirill Likhodedov
 */
public class GitLogProvider implements VcsLogProvider {

  private static final Logger LOG = Logger.getInstance(GitLogProvider.class);

  @NotNull private final Project myProject;
  @NotNull private final GitRepositoryManager myRepositoryManager;
  @NotNull private final VcsLogRefSorter myRefSorter;

  public GitLogProvider(@NotNull Project project, @NotNull GitRepositoryManager repositoryManager) {
    myProject = project;
    myRepositoryManager = repositoryManager;
    myRefSorter = new GitLogRefSorter(myRepositoryManager);
  }

  @NotNull
  @Override
  public List<? extends VcsCommitDetails> readFirstBlock(@NotNull VirtualFile root, boolean ordered) throws VcsException {
    String[] params = { "HEAD", "--branches", "--remotes", "--tags", "--encoding=UTF-8", "--full-history", "--sparse",
                        "--max-count=" + VcsLogProvider.COMMIT_BLOCK_SIZE};
    if (ordered) {
      params = ArrayUtil.append(params, "--date-order");
    }
    return GitHistoryUtils.history(myProject, root, params);
  }

  @NotNull
  @Override
  public List<TimeCommitParents> readAllHashes(@NotNull VirtualFile root) throws VcsException {
    return GitHistoryUtils.readAllHashes(myProject, root);
  }

  @NotNull
  @Override
  public List<? extends VcsCommitMiniDetails> readMiniDetails(@NotNull VirtualFile root, @NotNull List<String> hashes) throws VcsException {
    return GitHistoryUtils.readMiniDetails(myProject, root, hashes);
  }

  @NotNull
  @Override
  public List<? extends VcsCommitDetails> readDetails(@NotNull VirtualFile root, @NotNull List<String> hashes) throws VcsException {
    return GitHistoryUtils.commitsDetails(myProject, root, hashes);
  }

  @NotNull
  @Override
  public Collection<VcsRef> readAllRefs(@NotNull VirtualFile root) throws VcsException {
    myRepositoryManager.waitUntilInitialized();
    GitRepository repository = myRepositoryManager.getRepositoryForRoot(root);
    if (repository == null) {
      LOG.error("Repository not found for root " + root);
      return Collections.emptyList();
    }

    Collection<GitLocalBranch> localBranches = repository.getBranches().getLocalBranches();
    Collection<GitRemoteBranch> remoteBranches = repository.getBranches().getRemoteBranches();
    Collection<VcsRef> refs = new ArrayList<VcsRef>(localBranches.size() + remoteBranches.size());
    for (GitLocalBranch localBranch : localBranches) {
      refs.add(new VcsRef(Hash.build(localBranch.getHash()), localBranch.getName(), VcsRef.RefType.LOCAL_BRANCH, root));
    }
    for (GitRemoteBranch remoteBranch : remoteBranches) {
      refs.add(new VcsRef(Hash.build(remoteBranch.getHash()), remoteBranch.getNameForLocalOperations(), VcsRef.RefType.REMOTE_BRANCH, root));
    }
    String currentRevision = repository.getCurrentRevision();
    if (currentRevision != null) { // null => fresh repository
      refs.add(new VcsRef(Hash.build(currentRevision), "HEAD", VcsRef.RefType.HEAD, root));
    }

    refs.addAll(readTags(root));
    return refs;
  }

  // TODO this is to be removed when tags will be supported by the GitRepositoryReader
  private Collection<? extends VcsRef> readTags(@NotNull VirtualFile root) throws VcsException {
    GitSimpleHandler tagHandler = new GitSimpleHandler(myProject, root, GitCommand.LOG);
    tagHandler.addParameters("--tags", "--no-walk", "--format=%H%d" + GitLogParser.RECORD_START_GIT, "--decorate=full");
    String out = tagHandler.run();
    Collection<VcsRef> refs = new ArrayList<VcsRef>();
    for (String record : out.split(GitLogParser.RECORD_START)) {
      if (!StringUtil.isEmptyOrSpaces(record)) {
        refs.addAll(RefParser.parseCommitRefs(record.trim(), root));
      }
    }
    return refs;
  }

  @NotNull
  @Override
  public VcsKey getSupportedVcs() {
    return GitVcs.getKey();
  }

  @NotNull
  @Override
  public VcsLogRefSorter getRefSorter() {
    return myRefSorter;
  }

}