commit 7bf7d1ac3b074ad416d63789daea78f5e63e2b30
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Mon Nov 14 18:50:04 2016 +0300

    [vcs]: change hierarchy  of flatSpeedSearch, implement separated popups

    * move FlatSpeedSearch to vcs-impl module from log;
    * create 2 derived popups for branch widget and for branches in log;
    * provide wrapper-class for branch actions group insetead of ActionGroup;
    * improve strategy of branch speed search: show repo specific branches
    only if no common branches/actions were matched with current pattern;