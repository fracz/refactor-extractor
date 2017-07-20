commit cb487bd6e6270f3b85f4433e5afe35404be38d78
Author: phpnut <phpnut@cakephp.org>
Date:   Fri Sep 12 05:11:34 2008 +0000

    More optimization refactoring.
    Fix cache but causing  object_map to be created and deleted on each request.
    Replacing function and method calls with better performing code.

    git-svn-id: https://svn.cakephp.org/repo/branches/1.2.x.x@7596 3807eeeb-6ff5-0310-8944-8be069107fe0