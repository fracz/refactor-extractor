commit b4b5d60f77fbcac420d773f6fa9bef1c6eebdfa9
Author: epriestley <git@epriestley.com>
Date:   Mon Aug 3 08:01:43 2015 -0700

    Fix 'key'/'type' swap in email reset / one-time-login controller

    Summary: Fixes T9046. These got swapped around during refactoring.

    Test Plan:
      - Used `bin/auth recover` prior to patch (failed).
      - Used `bin/auth recover` after patch (worked).

    Reviewers: joshuaspence, chad

    Reviewed By: chad

    Subscribers: epriestley

    Maniphest Tasks: T9046

    Differential Revision: https://secure.phabricator.com/D13778