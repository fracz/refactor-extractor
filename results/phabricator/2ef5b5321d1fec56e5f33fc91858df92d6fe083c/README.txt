commit 2ef5b5321d1fec56e5f33fc91858df92d6fe083c
Author: epriestley <git@epriestley.com>
Date:   Thu Oct 1 08:06:23 2015 -0700

    Move Drydock logs to PHIDs and increased structure

    Summary:
    Ref T9252. Several general changes here:

      - Moves logs to use PHIDs instead of IDs. This generally improves flexibility (for example, it's a lot easier to render handles).
      - Adds `blueprintPHID` to logs. Although you can usually figure this out from the leasePHID or resourcePHID, it lets us query relevant logs on Blueprint views.
      - Instead of making logs a top-level object, make them strictly a sub-object of Blueprints, Resources and Leases. So you go Drydock > Lease > Logs, etc., to get to logs.
        - I might restore the "everything" view eventually, but it doesn't interact well with policies and I'm not sure it's very useful. A policy-violating `bin/drydock log` might be cleaner.
      - Policy-wise, we always show you that logs exist, we just don't show you log content if it's about something you can't see. This is similar to seeing restricted handles in other applications.
      - Instead of just having a message, give logs "type" + "data". This will let logs be more structured and translatable. This is similar to recent changes to Herald which seem to have worked well.

    Test Plan:
    Added some placeholder log writes, viewed those logs in the UI.

    {F855199}

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T9252

    Differential Revision: https://secure.phabricator.com/D14196