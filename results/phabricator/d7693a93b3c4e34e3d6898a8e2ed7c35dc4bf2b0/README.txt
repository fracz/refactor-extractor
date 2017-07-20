commit d7693a93b3c4e34e3d6898a8e2ed7c35dc4bf2b0
Author: epriestley <git@epriestley.com>
Date:   Tue Dec 15 06:57:32 2015 -0800

    Provide "Change Projects" and "Change Subscribers" (instead of "Add ...") in comment actions

    Summary:
    Ref T9908. Fixes T6205.

    This is largely some refactoring to improve the code. The new structure is:

      - Each EditField has zero or one "submit" (normal edit form) controls.
      - Each EditField has zero or one "comment" (stacked actions) controls.
        - If we want more than one in the future, we'd just add two fields.
      - Each EditField can have multiple EditTypes which provide Conduit transactions.
      - EditTypes are now lower-level and less involved on the Submit/Comment pathways.

    Test Plan:
      - Added and removed projects and subscribers.
      - Changed task statuses.
      - In two windows: added some subscribers in one, removed different ones in the other. The changes did not conflict.
      - Applied changes via Conduit.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T6205, T9908

    Differential Revision: https://secure.phabricator.com/D14789