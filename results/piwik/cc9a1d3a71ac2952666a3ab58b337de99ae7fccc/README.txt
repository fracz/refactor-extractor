commit cc9a1d3a71ac2952666a3ab58b337de99ae7fccc
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Thu Apr 1 21:08:24 2010 +0000

     * Added General settings page to: enable browser archiving, define today's archive time to live (fixes #1149)
     * added User settings page, that all logged in users can access to edit: Alias, email, default report to load, default date. Implemented settings in the _option table (rather than adding new fields to the table). Fixes #401 refs #526
     * the super user can edit settings for the anonymous user and force the anonymous to the login screen or a specific website
     * added order feature in admin menu + colored current menu + hiding menu entries that are not visible to the logged in user anyway
     * improved styles of admin forms

    git-svn-id: http://dev.piwik.org/svn/trunk@2036 59fd770c-687e-43c8-a1e3-f5a4ff64c105