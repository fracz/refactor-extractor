commit c53ffc30cbb56d53c9bde6f8b27517bfefad9c4d
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Mon May 31 11:57:21 2010 +0000

    Fixes #1351 All error messages displayed to screen/API should not be translated. Thanks JulienM for patch!

    I simply refactored the 'you must be have %s access [...]' messages by putting the admin/view/superuser as a parameter in the string

    git-svn-id: http://dev.piwik.org/svn/trunk@2246 59fd770c-687e-43c8-a1e3-f5a4ff64c105