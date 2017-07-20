commit 2224157f543c7c05ff100f46c091680a6dd7c823
Author: robocoder <anthon.pang@gmail.com>
Date:   Tue Jun 16 05:02:21 2009 +0000

    refs #803 - Log/Exception contains classes that won't be found by
    autoloader's discovery algorithm; FrontController.php and Common.php
    use classes with global scope functions (i.e., this needs to be
    refactored)


    git-svn-id: http://dev.piwik.org/svn/trunk@1232 59fd770c-687e-43c8-a1e3-f5a4ff64c105