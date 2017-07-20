commit 3e15e0b9806b1207b0cd9f5f7b7217eb9b71a706
Author: epriestley <git@epriestley.com>
Date:   Tue Nov 1 12:55:27 2016 -0700

    Store more datetime information on Calendar transactions and improve rendering behaviors

    Summary:
    Fixes T11805. Depends on D16785. This generally tries to smooth out transactions:

      - All-day stuff now says "Nov 3" instead of "Nov 3 12:00:00 AM".
      - Fewer weird bugs / extra transactions.
      - No more silly extra "yeah, you definitely set that event time" transaction on create.

    Test Plan: Edited events; changed from all-day to not-all-day and back again, viewed transaction log.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T11805

    Differential Revision: https://secure.phabricator.com/D16786