commit 1d5ace45bd11b5e6a70c04b87bbae2fe138bf8ce
Author: epriestley <git@epriestley.com>
Date:   Tue Dec 11 14:00:07 2012 -0800

    Add mail support to generic transactions

    Summary:
      - Adds mail support to the generic transaction construct.
      - Restores mail support to Pholio (now much improved; the mails are actually useful).

    Test Plan: Updated a Pholio mock, got mail.

    Reviewers: btrahan, chad, vrana

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T2104

    Differential Revision: https://secure.phabricator.com/D4139