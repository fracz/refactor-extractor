commit 9f1325af46313bf90dc0ba9f2061873d1c396384
Author: benakamoorthi <benaka@piwik.org>
Date:   Fri Feb 1 23:01:01 2013 +0000

    Refs #3619, several improvements/modifications to Referrers overview, including:

    * Add Total referrer metrics to evolution graph which are always displayed.
    * Added evolution percentage next to sparklines.
    * Moved other sparklines (for distinct metrics) to smaller section that slides down.

    Notes:
    * Fixed bug in broadcast.js where '+' chars were removed when sanitizing query params.
    * Moved multisites evolution & previous period calculating code to Piwik core.
    * Added section-toggler-link code to common js code (CoreHome/misc.js).
    * Refactored evolution percentage code used for row evolution popup.



    git-svn-id: http://dev.piwik.org/svn/trunk@7839 59fd770c-687e-43c8-a1e3-f5a4ff64c105