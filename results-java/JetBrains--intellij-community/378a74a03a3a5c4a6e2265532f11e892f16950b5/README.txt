commit 378a74a03a3a5c4a6e2265532f11e892f16950b5
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Mon Apr 18 21:20:46 2016 +0300

    refactor: convert VcsDirtyScopeVfsListener to project component

    The listener needs the per-project VcsDirtyScopeManager instance
    to mark files dirty, so just refer to it instead of identifying
    projects for each file and keeping maps of VDSM -> DirtyFiles.