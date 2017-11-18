package com.intellij.vcs.log;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Provides the information needed to build the VCS log, such as the list of most recent commits with their parents.
 *
 * @author Kirill Likhodedov
 */
public interface VcsLogProvider {

  int COMMIT_BLOCK_SIZE = 1000;

  /**
   * Reads {@link #COMMIT_BLOCK_SIZE the first part} of the log.
   */
  @NotNull
  List<? extends VcsCommitDetails> readFirstBlock(@NotNull VirtualFile root) throws VcsException;

  /**
   * Reads the whole history, but only hashes & parents.
   */
  @NotNull
  List<? extends CommitParents> readAllHashes(@NotNull VirtualFile root) throws VcsException;

  /**
   * Reads the whole history with hashes, parents and only those details which are necessary to display the log table: the commit subject,
   * the author and the time.
   */
  @NotNull
  List<? extends VcsCommit> readAllMiniDetails(@NotNull VirtualFile root) throws VcsException;

  /**
   * Read details of the given commits from the VCS.
   */
  @NotNull
  List<? extends VcsCommitDetails> readDetails(@NotNull VirtualFile root, @NotNull List<String> hashes) throws VcsException;

  /**
   * Read all references (branches, tags, etc.) for the given roots.
   */
  Collection<Ref> readAllRefs(@NotNull VirtualFile root) throws VcsException;
}