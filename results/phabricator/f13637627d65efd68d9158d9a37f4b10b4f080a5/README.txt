commit f13637627d65efd68d9158d9a37f4b10b4f080a5
Author: epriestley <git@epriestley.com>
Date:   Fri Mar 24 08:17:08 2017 -0700

    Improve daemon "waiting" message, config reload behavior

    Summary:
    Ref T12298. Two minor daemon improvements:

      - Make the "waiting" message reflect hibernation.
      - Don't trigger a reload right after launching.

    Test Plan:
    - Read "waiting" message.
    - Ran "bin/phd start", didn't see an immediate SIGHUP in the log.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T12298

    Differential Revision: https://secure.phabricator.com/D17550