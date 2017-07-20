commit 953ff197bf26db7d5e545f27d2c8f68d99255386
Author: epriestley <git@epriestley.com>
Date:   Sun Oct 6 17:10:29 2013 -0700

    Allow Herald rules to be disabled, instead of deleted

    Summary:
    Ref T603. Ref T1279. Further improves transaction and policy support for Herald.

      - Instead of deleting rules (which wipes out history and can't be undone) allow them to be disabled.
      - Track disables with transactions.
      - Gate disables with policy controls.
      - Show policy and status information in the headers.
      - Show transaction history on rule detail screens.
      - Remove the delete controller.
      - Support disabled queries in the ApplicationSearch.

    Test Plan:
      - Enabled and disabled rules.
      - Searched for enabled/disabled rules.
      - Verified disabled rules don't activate.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T1279, T603

    Differential Revision: https://secure.phabricator.com/D7247