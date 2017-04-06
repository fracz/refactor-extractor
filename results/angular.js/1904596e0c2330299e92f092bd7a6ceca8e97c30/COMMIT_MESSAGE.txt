commit 1904596e0c2330299e92f092bd7a6ceca8e97c30
Author: Ali Mills <amills@google.com>
Date:   Fri Jun 1 14:50:01 2012 -0700

    fix($timeout): allow calling $timeout.cancel() with undefined

    This is how it worked in rc9, before refactoring $defer into $timeout.