commit 4af6f49b2855646c2a2ebb2b67c0247bf01567a7
Author: peterbo <peter.boehlke@googlemail.com>
Date:   Mon Jun 6 09:30:56 2011 +0000

    Fixes #5 - refactored the display "last run" and "next scheduled run" method. - Since the Scheduled Tasks Timetable is not immediately updated when the plugin is enabled, we can not rely on that value. Now the displayed times should be correct and we don't have to compensate the not yet set Schedule-Timetable value with the "not yet rescheduled" phrase.

    git-svn-id: http://dev.piwik.org/svn/trunk@4884 59fd770c-687e-43c8-a1e3-f5a4ff64c105