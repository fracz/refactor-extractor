commit 0fa5b87fddc10ce487299c10fd3a833962058959
Author: Robert Muir <rmuir@apache.org>
Date:   Mon Feb 23 17:07:46 2015 -0500

    Add missing @Override annotations.

    These help a lot when refactoring, upgrading lucene, etc, and
    can prevent code duplication (as you get a compile error for outdated stuff).

    Closes #9832.