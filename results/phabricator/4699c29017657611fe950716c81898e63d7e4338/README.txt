commit 4699c29017657611fe950716c81898e63d7e4338
Author: epriestley <git@epriestley.com>
Date:   Thu Apr 30 17:09:45 2015 -0700

    Fix calendar z-index and Conpherence/Calendar integration

    Summary:
    Fixes T7975. Long ago, this element looked like this when you expanded it:

    ```
    +-------------------+
    |     3 4 5 6 7   X |
    | 8 9 1 2 3 4 5 +---+
    | 6 7 8 9 1 2 3 |
    | 4 5 6 7       |
    +---------------+
    ```

    That was why the icon needed a z-index. See T5880. @chad fixed this a while ago so it looks like this:

    ```
    +---------------+
    |     3 4 5 6 7 | X
    | 8 9 1 2 3 4 5 |
    | 6 7 8 9 1 2 3 |
    | 4 5 6 7       |
    +---------------+
    ```

    ...but we never stripped the z-index off, causing the bug in T7975.

    Also fix some collateral damage from the recent calendar refactoring and the Conpherence widget.

    Test Plan:
      - Created a new event via Conpherence
      - Created a new event normally.
      - Browsed a typeahead in Calendar without icons showing through.

    Reviewers: lpriestley, chad, btrahan

    Reviewed By: btrahan

    Subscribers: chad, epriestley

    Maniphest Tasks: T7975

    Differential Revision: https://secure.phabricator.com/D12639