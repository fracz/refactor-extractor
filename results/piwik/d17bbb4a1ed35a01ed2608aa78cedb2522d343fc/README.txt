commit d17bbb4a1ed35a01ed2608aa78cedb2522d343fc
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Tue Mar 30 12:36:04 2010 +0000

    Fixes #1001 Adding currency setting per website
    Defaulting currency to USD for all existing websites
    Simplifying website admin screen by rolling all settings into the same form (still using ajax)
    Adding SitesManager and UsersManager to the list of hidden plugins
    Slightly refactored printing money values to accomodate per website preference

    git-svn-id: http://dev.piwik.org/svn/trunk@2020 59fd770c-687e-43c8-a1e3-f5a4ff64c105