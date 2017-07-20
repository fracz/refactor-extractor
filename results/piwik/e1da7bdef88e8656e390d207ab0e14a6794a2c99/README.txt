commit e1da7bdef88e8656e390d207ab0e14a6794a2c99
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Mon Jul 28 00:37:19 2008 +0000

    - refactoring exception and error message output: now always the nice output will be used (with piwik logos, etc.)
    - redirecting to piwik.org through an html page, so we lose the referer, to make sure we can't get any token_auth in the referer when going to piwik.org

    git-svn-id: http://dev.piwik.org/svn/trunk@583 59fd770c-687e-43c8-a1e3-f5a4ff64c105