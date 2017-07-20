commit 9515909a5c0c1187cb9feb6adb60ddb8d30c8045
Author: Stefan Giehl <stefan@piwik.org>
Date:   Tue Sep 20 05:26:28 2016 +0200

    Fix Scheduled Reports sent one hour late in daylight saving timezones (#10443)

    * convert hour to send report to/from UTC, to ensure it isn't affected by daylight savings

    * adds update script to change existing scheduled reports to use utc time

    * code improvement

    * adds missing param