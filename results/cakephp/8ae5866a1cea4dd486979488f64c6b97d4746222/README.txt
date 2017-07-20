commit 8ae5866a1cea4dd486979488f64c6b97d4746222
Author: phpnut <phpnut@cakephp.org>
Date:   Thu Sep 18 03:09:19 2008 +0000

    Additional optimization refactoring
    Removed ability to use deprecated / with plugin, helper, etc combinations. The dot notation is only allowed from this point forward.
    Corrected tests for the above changes.
    Corrected formating in basics.php

    git-svn-id: https://svn.cakephp.org/repo/branches/1.2.x.x@7623 3807eeeb-6ff5-0310-8944-8be069107fe0