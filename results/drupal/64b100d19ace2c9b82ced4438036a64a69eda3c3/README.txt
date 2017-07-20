commit 64b100d19ace2c9b82ced4438036a64a69eda3c3
Author: Dries Buytaert <dries@buytaert.net>
Date:   Sun Jan 9 09:22:40 2005 +0000

    - Patch #13260 by UnConeD: watchdog module improvements.

      We added a 'severity' column to watchdog():
        watchdog($type, $message, $link) --> watchdog($type, $message, $severity, $link);

        * Specify a severity in case you are reporting a warning or error.
        * The $link-parameter is now the fourth parameter instead of the third.

      TODO: document this in the upgrade guide.