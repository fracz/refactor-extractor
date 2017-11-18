commit 5391362bbf18067fa8237b0535eaa72c27d0df37
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Fri Sep 13 18:30:33 2013 +0400

    [log] Sort reference labels

    Use VcsLogRefSorter to sort references displayed near the commit in
    the log table; in the branches panel; in the details panel.

    Implement GitLogRefSorter to add more priority to HEAD,
    master; local branches over remote branches and tags; tracked remote
    branches over non-tracked.
    Add a test.

    In the BranchesPanel show only tracked remote branches.

    Show refs from different roots together, one by one. This is to be
    improved later.