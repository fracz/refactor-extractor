commit 0c658802cbb703035fe64024e84c8525e4e77861
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Sat Oct 10 00:15:23 2015 +0300

    [patch]: process SKIP status during resolving conflicts

    * add SKIP enum status;
    * refactor 'and' method using ordering from guava;
    * test added;
    * SKIP may appear only for modified files and means continue apply patch action for next patch-file in current base;