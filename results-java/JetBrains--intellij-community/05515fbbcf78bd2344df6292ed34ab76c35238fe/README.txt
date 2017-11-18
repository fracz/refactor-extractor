commit 05515fbbcf78bd2344df6292ed34ab76c35238fe
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Wed Oct 10 15:50:12 2012 +0400

    Git branches refactoring, tests and some minor fixes.

    REFACTORING:
    * Split the GitBrancher into 3 parts:
      - Extract all UI interactivity to the GitBranchUiHandler interface. Implement in GitBranchUiHandlerImpl.
      - Extract firing a background task into GitBrancher, which would be the public interface for branch operations.
      - Put the actual work into GitBranchWorker.
      - Remove the result handler from those GitBrancher methods which had it, until it is clear in what situation we need this result handler.
    * Extract deletion of the remote branch into GitDeleteRemoteBranchOperation.
    * Several improvements to lessen the dependency from platform:
      - move convertPathsToChanges from GitUtil to GitBranchOperation: it is used only there.
      - PlatformFacade.getVirtualFileForPath(). This is not the best idea, but other would be too much effort for now.
      - Use Git interface instead of direct GitHandler usage in several cases (stash save and pop, diff, history).
    * Pass the PlatformFacade in many many Git components and classes instead of calling <platform component>.getInstance().

    FIX a few bugs that were discovered during refactoring & testing:
    * in multi-repository setup, if deletion succeeded for the first repository, and was denied for the second, don't collect information from already deleted branches.
    * if a branch is not fully merged, and user denies force deletion, don't show notification about deletion failure.
    * Make update of repository info (current revison, branch, etc.) before any active branch operation to make sure we perform operations based on actual information.

    TESTING:
    * Rewrite GitBranchOperationsTest to GitBranchWorkerTest instead that is platform independent and fast.
    * Enhance GitExecutor with more commands useful to work with the file system.
    * Introduce GitScenarios that would create some popular scenarios used in multiple tests, e. g.:
      - create a branch and commit something there;
      - make some unmerged files in the working tree;
      - make the situation when local changes would be overwritten by merge
    * Transform GitTestPlatformFacade, GitTestRepositoryManager to Groovy.
    * GitMockVirtualFile: use a single instance of the file system. Otherwise files comparison fails in some cases.