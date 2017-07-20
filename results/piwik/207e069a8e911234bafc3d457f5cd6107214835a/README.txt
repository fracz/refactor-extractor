commit 207e069a8e911234bafc3d457f5cd6107214835a
Author: robocoder <anthon.pang@gmail.com>
Date:   Mon Aug 9 10:25:15 2010 +0000

    fixes #1419 - hash token_auth in the login cookie; I'm deferring implementation of the Miller/Jaspan recommendations for the time being (at least until we've looked at #906 OAuth and have a better sense of what needs to be refactored)

    git-svn-id: http://dev.piwik.org/svn/trunk@2904 59fd770c-687e-43c8-a1e3-f5a4ff64c105