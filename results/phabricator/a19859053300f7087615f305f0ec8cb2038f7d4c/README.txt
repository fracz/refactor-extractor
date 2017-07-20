commit a19859053300f7087615f305f0ec8cb2038f7d4c
Author: epriestley <git@epriestley.com>
Date:   Fri Jun 23 12:06:04 2017 -0700

    Degrade more gracefully when ProfileMenu dashboards fail to render

    Summary:
    Ref T12871. This replaces a dead end UI (user totally locked out) with one where the menu is still available, if the default menu item is one which generates a policy exception (e.g., because users can't see the dashboard).

    Really, we should do better than this and not select this item as the default item if the viewer can't see it, but there is currently no reliable way to test for "can the viewer see this item?" so this is a more involved change. I'm thinking we get this minor improvement into the release, then pursue a more detailed fix afterward.

    Test Plan:
      - Added a dashboard as the top item in the global menu.
      - Changed the dashboard to be visible to only user B.
      - Viewed Home as user A.
      - Before patch: entire page is a policy exception dialog.
      - After patch, things are better:

    {F5014179}

    Reviewers: chad, amckinley

    Reviewed By: amckinley

    Maniphest Tasks: T12871

    Differential Revision: https://secure.phabricator.com/D18152