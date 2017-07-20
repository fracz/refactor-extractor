commit 58b55c2fa6d4c7f600cee469a78b59ddb26e1a41
Author: epriestley <git@epriestley.com>
Date:   Tue Apr 26 16:27:31 2016 -0700

    Probably improve behavior around duplicate notifications

    Summary:
    We're sometimes getting duplicate notifications right now. I think this is because multiple windows are racing and becoming leaders.

    Clean this up a little:

      - Fix the `timeoout` typo.
      - Only try to usurp once.
      - Use different usurp and expire delays, so we don't fire them at the exact same time.

    Not sure if this'll work or not but it should theoretically be a little cleaner.

    Test Plan:
      - Quit Safari, reopened Safari, still saw a fast reconnect to the notification server (this is the goal of usurping).
      - Did normal notification stuff like opening a chat in two windows, got notifications.
      - Hard to reproduce the race for sure, but this at least fixes the outright `timeoout` bug.

    Reviewers: chad

    Reviewed By: chad

    Differential Revision: https://secure.phabricator.com/D15806