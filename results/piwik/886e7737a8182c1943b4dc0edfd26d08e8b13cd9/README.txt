commit 886e7737a8182c1943b4dc0edfd26d08e8b13cd9
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Tue Aug 14 20:50:20 2007 +0000

    - reordered files
    - implemented half of the day archiving process (tables partitioning is OK, referers archiving is OK, simple stats OK)
    - modified the DataTable internals
    - refactored some common code of LogStats and Core
    - fixed bug in the error handler that caused notice not to be displayed
    - improved message logging so we can print arrays

    git-svn-id: http://dev.piwik.org/svn/trunk@39 59fd770c-687e-43c8-a1e3-f5a4ff64c105