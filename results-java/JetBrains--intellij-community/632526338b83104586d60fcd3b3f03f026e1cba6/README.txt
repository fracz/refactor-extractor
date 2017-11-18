commit 632526338b83104586d60fcd3b3f03f026e1cba6
Author: Maxim Shafirov <max@jetbrains.com>
Date:   Sun Oct 7 16:01:49 2007 +0400

    [no review]: IDEADEV-22191: Do not perform redundant imports removal in JSP files after refactoring. This doesn't work properly with imports, which reside in included files.