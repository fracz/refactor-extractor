commit f3efdff459dba8ba66c6990b8729703db82065b2
Author: Misagh Moayyed <mmoayyed@unicon.net>
Date:   Sat Oct 21 22:55:10 2017 -0700

    Mgmt: fix issue with the angular router crashing on homepage refreshes (#3020)

    * Closes Issue #35 - Page refresh causes app to be unavailable

    * Added response back to fix tests

    * Setup app-controls, UI improvements, made more responsive

    * Fixed Form save to use the controls component

    * Refactored datasource functionality

    * fix Checkstyle errors