commit cc4b4016e4b86db012f94bb889e5ca61ff362171
Author: Robert Greenwalt <robdroid@android.com>
Date:   Mon Jan 25 17:54:29 2010 -0800

    Fix the reporting of NO_CONNECTIVITY.

    A refactoring of handleDisconnect instroduced a bug - we were reporting
    NO_CONNECTIVITY after any non-primary network (supl, mms, hipri) was lost.

    bug:2395006

    Change-Id: Ifa9e008872ec646981a35f2c316120cb9685a6a4