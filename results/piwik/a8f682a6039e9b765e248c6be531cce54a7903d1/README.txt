commit a8f682a6039e9b765e248c6be531cce54a7903d1
Author: Matthieu Aubry <mattab@users.noreply.github.com>
Date:   Mon Sep 26 16:26:55 2016 +1300

    Release 2.16.3-b4 (#10558)

    * Fix Scheduled Reports sent one hour late in daylight saving timezones (#10443)

    * convert hour to send report to/from UTC, to ensure it isn't affected by daylight savings

    * adds update script to change existing scheduled reports to use utc time

    * code improvement

    * adds missing param

    * Added new event Archiving.makeNewArchiverObject to  allow customising plugin archiving  (#10366)

    * added hook to alllow plugin archiving prevention

    * cr code style notes

    * reworked PR to fit CR suggestions

    * added PHPDoc for hook

    * Event description more consistent

    * UI tests: minor changes

    * Adds test checking if all screenshots are stored in lfs

    * removed screenshots not stored in lfs

    * readds screenshots to lfs

    * 2.16.3-b4