commit 5185859a353de1daa6d5824fa09a7a7c10c32280
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Tue Oct 28 16:05:33 2008 +0000

    - you can now delete alias URLs using the UI
    - improved installation help messages + auto focus on form fields
    - simplified sitesmanager API (deleted replaceUrls method)
    - ts_created fields in site table is now NOT CURRENT_TIMESTAMP ON UPDATE (I can't believe this is default!!! BAD mysql)

    git-svn-id: http://dev.piwik.org/svn/trunk@654 59fd770c-687e-43c8-a1e3-f5a4ff64c105