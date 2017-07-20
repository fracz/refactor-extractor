commit e0507d715f281f56e10d0b87dc4e013f3aba96c1
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Mon Mar 30 02:22:03 2009 +0000

    - fixing recently introduced sorting issue, refactoring, cleaning up the generic filters concept by removing the automatic sorting of data
    - fixing issue when executing unit tests would invalidate tmp/cache/tracker files
    - fixed edge case issue when calling several apis from one http request, and requesting recursive output, it was failing in some random cases
    - adding a unit test that calls all callable api methods and check for non empty output
    -

    git-svn-id: http://dev.piwik.org/svn/trunk@1041 59fd770c-687e-43c8-a1e3-f5a4ff64c105