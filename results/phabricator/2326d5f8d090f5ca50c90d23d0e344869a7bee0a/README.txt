commit 2326d5f8d090f5ca50c90d23d0e344869a7bee0a
Author: epriestley <git@epriestley.com>
Date:   Mon Oct 26 20:00:49 2015 +0000

    Show lease on Repository Operation detail view and awaken on failures

    Summary:
    Ref T182. Couple of minor improvements here:

      - Show the Drydock lease when viewing a Repository Operation detail screen. This just makes it easier to jump around between relevant objects.
      - When tasks are waiting for a lease, awaken them when it breaks or is released, not just when it is acquired. This makes the queue move forward faster when errors occur.

    Test Plan:
      - Viewed a repository operation and saw a link to the lease.
      - Did a bad land (intentional merge problem) and got an error in about ~3 seconds instead of ~17.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T182

    Differential Revision: https://secure.phabricator.com/D14341