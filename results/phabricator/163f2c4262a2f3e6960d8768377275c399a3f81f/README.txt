commit 163f2c4262a2f3e6960d8768377275c399a3f81f
Author: epriestley <git@epriestley.com>
Date:   Thu Jun 30 10:51:14 2016 -0700

    Refine available filters and defaults for relationship selection

    Summary:
    Ref T4788. Fixes T10703.

    In the longer term I want to put this on top of ApplicationSearch, but that's somewhat complex and we're at a fairly good point to pause this feature for feedback.

    Inch toward that instead: provide more appropriate filters and defaults without rebuilding the underlying engine. Specifically:

      - No "assigned" for commits (barely makes sense).
      - No "assigned" for mocks (does not make sense).
      - Default to "open" for parent tasks, subtasks, close as duplicate, and merge into.

    Also, add a key to the `search_document` table to improve the performance of the "all open stuff of type X" query. "All Open Tasks" is about 100x faster on my machine with this key.

    Test Plan:
      - Clicked all object relationships, saw more sensible filters and defaults.
      - Saw "open" query about 100x faster locally (300ms to 3ms).

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T4788, T10703

    Differential Revision: https://secure.phabricator.com/D16202