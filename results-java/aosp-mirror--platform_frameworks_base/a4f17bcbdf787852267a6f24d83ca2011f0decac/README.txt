commit a4f17bcbdf787852267a6f24d83ca2011f0decac
Author: Hugo Benichi <hugobenichi@google.com>
Date:   Mon Nov 21 13:50:05 2016 +0900

    DO NOT MERGE Captive portal systel log improvements

    This patch improves system logging around captive portal detection to
    make inspection of bug reports sligthly easier:

    - NetworkMonitor now logs by default CMD_CAPTIVE_PORTAL_RECHECK and
      CMD_CAPTIVE_PORTAL_APP_FINISHED. Other system logs are kept off with
      a new VDBG boolean contant,
    - NetworkNotificationManager now prints the notification id at
      notification time. This allows to easily correlate show and clear.
    - errors in NetworkNotificationManager are logged as Throwable instead
      of through their implicit toString() method.

    Test: $ runtest frameworks-net
    Bug: 32198726

    (cherry picked from commit 8b025bf108c729156b40159038befa0e6c5bebce)

    Change-Id: I7780c389a94c4b9fa226f53b02fe5960d1c08618