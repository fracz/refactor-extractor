commit 7f088d14828e19181fc01c7a3e7f0f33ce0b1386
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Mon Mar 22 11:26:54 2010 +0000

    Fixes #43
    - modify websites admin UI, API to add a column Exclude IPs
    - IPs contain wildcards, unlimited IPs per website
    - below the website table, added a "Global IP exclude" list. Ips there are excluded from all websites automatically.
    - IPs are stored in the tracker cache file for fast access at Tracking time.
    - added new field in website table "excluded_ips"
    - refactored the ajax loading/error display to allow multiple loading/error div per page


    git-svn-id: http://dev.piwik.org/svn/trunk@1970 59fd770c-687e-43c8-a1e3-f5a4ff64c105