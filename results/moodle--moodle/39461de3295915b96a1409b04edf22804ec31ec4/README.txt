commit 39461de3295915b96a1409b04edf22804ec31ec4
Author: Petr Skoda <skodak@moodle.org>
Date:   Mon Oct 25 20:44:32 2010 +0000

    MDL-24837 improved user preferences internal implementation, general code cleanup, minor bugfixing, fixed '0' value regression and improved caching options in cron scripts (this should hopefully help with messaging performance in cron once it is used there), implemented basic unit tests for user user preferences