commit 965a2058d096a680d752adf488b603be60706a73
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Tue Apr 19 03:25:45 2011 +0000

    Improvements to getSitesIdFromSiteUrl() - now normalizing URLs to match the www. and plain host version when querying the API
    We should try and make all URLs matching code in Piwik do such normalization (refactor this one if need be?)

    git-svn-id: http://dev.piwik.org/svn/trunk@4507 59fd770c-687e-43c8-a1e3-f5a4ff64c105