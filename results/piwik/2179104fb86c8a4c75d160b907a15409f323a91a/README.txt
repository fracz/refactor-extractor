commit 2179104fb86c8a4c75d160b907a15409f323a91a
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Mon Mar 23 02:41:16 2009 +0000

    - improved installation code, increased security checks & feedback to user, in case user tries to install and conflicting tables are already found in the DB
    - fixed #612 Error when no sites configured
    - fixed Base table or view not found doesn't exist piwik_option bug that was submitted by several users

    git-svn-id: http://dev.piwik.org/svn/trunk@1006 59fd770c-687e-43c8-a1e3-f5a4ff64c105