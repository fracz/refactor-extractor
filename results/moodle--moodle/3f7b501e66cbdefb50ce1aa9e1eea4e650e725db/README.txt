commit 3f7b501e66cbdefb50ce1aa9e1eea4e650e725db
Author: Sam Hemelryk <sam@moodle.com>
Date:   Wed May 23 09:34:23 2012 +1200

    MDL-31414 mod_assign: Code cleanup for notification improvements

    * No need to manually call update of providers that happens automatically.
    * Added upgrade code for the new sendlatenotifications field.
    * Added AMOS syntax for the two renamed strings
    * Refactored assign::cron to use half as many DB queries
    * Cleaned up unused vars and globals, coding style and phpdocs.
    * Removed string notifications added previously but never used.

    AMOS BEGIN
       MOV [emailgradermail,mod_assign],[gradersubmissionupdatedtext,mod_assign]
       MOV [emailgradermailhtml,mod_assign],[gradersubmissionupdatedhtml,mod_assign]
    AMOS END