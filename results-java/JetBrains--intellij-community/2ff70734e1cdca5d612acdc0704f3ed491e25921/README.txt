commit 2ff70734e1cdca5d612acdc0704f3ed491e25921
Author: Dmitry Batrak <Dmitry.Batrak@jetbrains.com>
Date:   Fri Jul 14 19:19:45 2017 +0300

    reorganize code showing parameter hints on completion

    Both completion and 'static' hints are generated in one place now -
    by hints provider.
    This should also fix IDEA-173963.