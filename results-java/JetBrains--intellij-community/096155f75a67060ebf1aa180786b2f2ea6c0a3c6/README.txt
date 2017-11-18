commit 096155f75a67060ebf1aa180786b2f2ea6c0a3c6
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Wed Mar 26 19:44:43 2014 +0400

    [log] refactor & some API changes

    Use a special interface to pass necessary parameters (the need of a
    separate interface rather than just several parameters will be proved
    later.

    Move commits sorting stuff to GitLogProvider, since it is needed only
    for Git.

    Make VcsShortCommitDetails extend TimedVcsCommit. Although
    ideologically this is incorrect (details are not a commit, they lay
    "near" the commit), this is useful for stuff like commits sorting to
    avoid convertations & map creation.