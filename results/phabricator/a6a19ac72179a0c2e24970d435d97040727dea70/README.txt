commit a6a19ac72179a0c2e24970d435d97040727dea70
Author: epriestley <git@epriestley.com>
Date:   Wed Apr 2 12:05:07 2014 -0700

    Move "Delete User" action to user profiles

    Summary:
    Ref T4065. The existence of two separate edit workflows for users is broadly confusing to administrators.

    I want to unify user administration and improve administration of system agent accounts. Particularly, I plan to:

      - Give administrators limited access to profile editing of system agents (e.g., change profile picture).
      - Give administrators limited access to Settings for system agents.
      - Broadly, move all the weird old special editing into standard editing.

    Test Plan:
      - Hit all the errors (delete self, no username, wrong username).
      - Deleted a user.
      - Visited page as a non-admin, got 403'd.
      - Viewed old edit UI.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T4065

    Differential Revision: https://secure.phabricator.com/D8662