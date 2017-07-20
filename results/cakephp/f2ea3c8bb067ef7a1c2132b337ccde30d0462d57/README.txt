commit f2ea3c8bb067ef7a1c2132b337ccde30d0462d57
Author: nate <nate@cakephp.org>
Date:   Thu May 22 19:05:37 2008 +0000

    Refactoring DboSource to use Model in order to properly quote values by column type, correcting serial integer schema generation in DboPostgres, fixes #4702, refactoring TreeBehavior to use DboSource::calculate(), fixes #4725, updating docblocks, fixing DboSource test to be database-agnostic, misc. whitespace fixes, updating DboSource test to reflect new quoting rules


    git-svn-id: https://svn.cakephp.org/repo/branches/1.2.x.x@7015 3807eeeb-6ff5-0310-8944-8be069107fe0