commit 62939c122a683de27ed714f3c1af2fa84700b0ef
Author: Thomas Steur <thomas.steur@googlemail.com>
Date:   Mon Mar 3 21:44:05 2014 +0100

    refs #57 do not flatten for now, brings massive performance improvement 10min > 1 sec as Referrers_getKeywords takes forever and also leads in memory exhausted when report is flattened