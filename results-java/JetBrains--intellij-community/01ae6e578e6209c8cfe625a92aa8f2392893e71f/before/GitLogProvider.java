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
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.vcs.log.*;
import git4idea.GitCommit;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.hanuna.gitalk.common.MyTimer;
import org.hanuna.gitalk.log.commit.parents.SimpleCommitParents;
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

  private final Project myProject;
  @NotNull private final GitRepositoryManager myRepositoryManager;

  public GitLogProvider(@NotNull Project project, @NotNull GitRepositoryManager repositoryManager) {
    myProject = project;
    myRepositoryManager = repositoryManager;
  }

  @NotNull
  @Override
  public List<CommitParents> readNextBlock(@NotNull VirtualFile root) throws VcsException {
    // TODO either don't query details here, or save them right away
    MyTimer timer = new MyTimer("Git Log ALL");
    List<GitCommit> history = GitHistoryUtils.history(myProject, root, "HEAD", "--branches", "--remotes", "--tags", "--date-order",
                                                      "--encoding=UTF-8", "--full-history", "--sparse",
                                                      "--max-count=" + VcsLogProvider.COMMIT_BLOCK_SIZE);
    List<CommitParents> map = ContainerUtil.map(history, new Function<GitCommit, CommitParents>() {
      @Override
      public CommitParents fun(GitCommit gitCommit) {
        return new SimpleCommitParents(Hash.build(gitCommit.getHash().toString()), gitCommit.getParents());
      }
    });
    timer.print();
    return map;
  }

  @NotNull
  @Override
  public List<CommitData> readCommitsData(@NotNull VirtualFile root, @NotNull List<String> hashes) throws VcsException {
    List<GitCommit> gitCommits;
    MyTimer timer = new MyTimer();
    timer.clear();
    gitCommits = GitHistoryUtils.commitsDetails(myProject, root, hashes);
    System.out.println("Details loading took " + timer.get() + "ms for " + hashes.size() + " hashes");

    List<CommitData> result = new SmartList<CommitData>();
    for (GitCommit gitCommit : gitCommits) {
      result.add(new CommitData(gitCommit));
    }
    return result;
  }

  @Override
  public Collection<? extends Ref> readAllRefs(@NotNull VirtualFile root) throws VcsException {
    GitRepository repository = myRepositoryManager.getRepositoryForRoot(root);
    if (repository == null) {
      LOG.error("Repository not found for root " + root);
      return Collections.emptyList();
    }

    // TODO tags
    Collection<GitLocalBranch> localBranches = repository.getBranches().getLocalBranches();
    Collection<GitRemoteBranch> remoteBranches = repository.getBranches().getRemoteBranches();
    Collection<Ref> refs = new ArrayList<Ref>(localBranches.size() + remoteBranches.size());
    for (GitLocalBranch localBranch : localBranches) {
      refs.add(new Ref(Hash.build(localBranch.getHash()), localBranch.getName(), Ref.RefType.LOCAL_BRANCH));
    }
    for (GitRemoteBranch remoteBranch : remoteBranches) {
      refs.add(new Ref(Hash.build(remoteBranch.getHash()), remoteBranch.getNameForLocalOperations(), Ref.RefType.REMOTE_BRANCH));
    }
    String currentRevision = repository.getCurrentRevision();
    if (currentRevision != null) { // null => fresh repository
      refs.add(new Ref(Hash.build(currentRevision), "HEAD", Ref.RefType.HEAD));
    }
    return refs;
  }
}