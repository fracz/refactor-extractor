commit 1c42fe934bc77e91f44a9a2708e7874242428f39
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Fri Jul 12 17:20:05 2013 +0400

    Log: Separate hash+parents from commit details; refactor initialization

    CommitParents: hash + parents
    VcsCommit: + "mini details" necessary for the log table (subject,
    author, time)
    VcsCommitDetails: "full details" displayed when a commit is selected:
     full message, changes, etc.

    Separate caches for mini- and full-details. Interit from the
    DataGetter for both, which substitutes the CommitDetailsGetter.

    Refactor initialization procedure:
    * load 1000 commits with details, show the UI.
    * then load ALL commits and save them in a List.