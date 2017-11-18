commit a9ddaaafa7ef4f733ca7971ba9af32feca4f6334
Author: Kirill Likhodedov <kirill.likhodedov@jetbrains.com>
Date:   Tue Feb 22 10:53:23 2011 +0300

    Git update totally rewritten

    Rewrite all code related to Git update (pull) procedures.
    Reason: poor code architecture, lack of user-friendly error handling.
    This commit introduces main architecture change, but there are many improvements to go.
    Also, push via GitPushActiveBranches doesn't work yet.

    GitUpdateProcess rules the update process.
    Update is separated in two stages: preserving local changes via GitChangesSaver and restoring changes after update; updating via 'git pull'.
    GitUpdateProcess also checks if update is not possible because of incomplete rebase/merge or other error and tries to provide user-friendly solution.

     GitChangesSaver and its implementations GitShelveChangesSaver and GitStashChangesSaver look for uncommitted changes and preserve them in shelf/stash. Changes are restored after update, but not if update was incomplete (when a merge conflict wasn't immediately resolved).

     GitUpdater and its implementations GitMergeUpdater and GitRebaseUpdater are responsible to update a single Git root via 'git pull' or 'git pull --rebase' respectively.

     GitRebaser is a utility class which handles rebase operations.