commit 32c3c359697bb837cfa4e1a380a823d811797709
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Sun Nov 3 19:26:35 2013 +0400

    [log] Add "New Branch", "New Tag" actions to the log + refactor

    * Extract GitLogSingleCommitAction - the common class
      for all Git actions on the log, which operate on a single commit.
    * Implement "New Branch" and "New Tag" actions. Use GitBrancher for
      the first, reuse the existing CreateNewTag for the latter.
    * Regroup actions in plugin.xml so that they form a single group which
      is then added to the Vcs group.
      It lets add a separator, and also makes the code more clear.