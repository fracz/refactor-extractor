commit cd2d68a6c85dedbe2061e966c65e763b9adb1100
Author: phpnut <phpnut@cakephp.org>
Date:   Sat Feb 4 08:13:59 2006 +0000

    Merging fixes and enhancements into trunk

    Revision: [1927]
    A little refactoring on the findBy<field> and findAllBy<field>.
    These should both work on PHP 4 now without an issue.
    Change the condition to be built as an array in DboMysql::query().
    Still want to refactor the DboSource::conditions() which is note around line 1058 TODO:

    git-svn-id: https://svn.cakephp.org/repo/trunk/cake@1928 3807eeeb-6ff5-0310-8944-8be069107fe0