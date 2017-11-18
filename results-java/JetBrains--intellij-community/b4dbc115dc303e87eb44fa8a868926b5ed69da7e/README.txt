commit b4dbc115dc303e87eb44fa8a868926b5ed69da7e
Author: Kirill Likhodedov <kirill.likhodedov@jetbrains.com>
Date:   Thu Dec 9 18:03:34 2010 +0300

    GitBranchWidget refactoring.
    Introduced GitBranches project service to keep and track branch information (for now only current branch per root).
    Introduced GitBranchesListener interface to listen to changes in branch configuration.
    Renamed GitCurrentBranchWidget to GitBranchWidget.
    Moved all 3 classes to git4idea.branch.
    Also fixed: if current file is unversioned, show nothing in the widget.