commit c4de87a07ab07e3ea27153e84e94905038155542
Author: epriestley <git@epriestley.com>
Date:   Sun Jun 5 07:47:05 2016 -0700

    Improve some settings-related performance

    Summary:
    Ref T4103. Two small improvements:

      - Don't work as hard to validate translations. We just need to know if a translation exists, we don't need to count how many strings it has and build the entire menu.
      - Allow `getUserSetting()` to work on any setting without doing all the application/visibility checks. It's OK for code to look at, say, your "Conpherence Notifications" setting even if that application is not installed for you.

    Test Plan: Used XHProf and saw 404 page drop from ~60ms to ~40ms locally.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T4103

    Differential Revision: https://secure.phabricator.com/D16046