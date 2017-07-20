commit 8a56a00488c79fe992d24279bf3935de2552b365
Author: robocoder <anthon.pang@gmail.com>
Date:   Mon Nov 16 01:11:41 2009 +0000

    refs #680 - improve usability of datepicker
     * updated from jquery calendar 2.7 to jquery-ui datepicker 1.7.2
     * contains a DST-related fix that might solve an issue of duplicate days reported by some users
     * fixes regression in UI where clicking on calendar icon would not collapse the calendar
     * implements some of the style changes shown in the mockup
     * updated the calendar text translations from the jquery-ui i18n


    git-svn-id: http://dev.piwik.org/svn/trunk@1587 59fd770c-687e-43c8-a1e3-f5a4ff64c105