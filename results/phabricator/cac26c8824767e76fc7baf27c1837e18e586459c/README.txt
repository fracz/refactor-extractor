commit cac26c8824767e76fc7baf27c1837e18e586459c
Author: epriestley <git@epriestley.com>
Date:   Tue May 3 04:20:35 2016 -0700

    Fix errant rules for associating projects when dragging tasks within a milestone column

    Summary:
    Fixes T10912. When you drag tasks within a milestone, we currently apply an overbroad, API-focused rule and add the parent board's project. This logic was added fairly recently, as part of T6027, to improve the behavior of API-originated moves.

    Later on, this causes the task to toggle in and out of the parent project on every alternate drag.

    This logic is also partially duplicated in the `MoveController`.

      - Add test coverage for this interaction.
      - Fix the logic so it accounts for subproject / milestone columns correctly.
      - Put all of the logic into the TransactionEditor, so the API gets the exact same rules.

    Test Plan:
      - Added a failing test and made it pass.
      - Dragged tasks around within a milestone column:
        - Before patch: they got bogus project swaps on every other move.
        - After patch: projects didn't change (correct).
      - Dragged tasks around between normal and milestone columns.
        - Before patch: worked properly.
        - After patch: still works properly.

    Here's what the bad changes look like, the task is swapping projects with every other move:

    {F1255957}

    The "every other" is because the logic was trying to do this:

      - Add both the parent and milestone project.
      - Whichever one exists already gets dropped from the change list because it would have no effect.
      - The other one then applies.
      - In applying, it forces removal of the first one.
      - Then this process repeats in the other direction the next time through.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T10912

    Differential Revision: https://secure.phabricator.com/D15834