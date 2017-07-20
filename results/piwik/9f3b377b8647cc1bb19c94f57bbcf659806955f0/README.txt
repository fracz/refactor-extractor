commit 9f3b377b8647cc1bb19c94f57bbcf659806955f0
Author: robocoder <anthon.pang@gmail.com>
Date:   Sun May 9 07:42:31 2010 +0000

    fixes #1345 - for curl, use CONNECTTIMEOUT (timeout to connect) instead of TIMEOUT (time to complete curl operation) as the .zip file has increased in size, and the server (and/or network) seems slower; improve error checking

    git-svn-id: http://dev.piwik.org/svn/trunk@2166 59fd770c-687e-43c8-a1e3-f5a4ff64c105