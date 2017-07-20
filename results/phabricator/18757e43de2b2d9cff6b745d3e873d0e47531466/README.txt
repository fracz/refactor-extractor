commit 18757e43de2b2d9cff6b745d3e873d0e47531466
Author: epriestley <git@epriestley.com>
Date:   Thu Jun 12 21:49:19 2014 -0700

    Allow entire dashboards to be copied

    Summary:
    Further improve UX for dealing with policy rules on dashboards:

      - When in the "Manage" view of a dashboard you can not edit:
        - Don't show the panel management controls.
        - Show a notice that the board isn't editable, recommending you make a copy instead.
      - Add a "Copy Dashboard" action to create a copy which you //can// edit.

    Test Plan: Copied some dashboards. See screenshots.

    Reviewers: chad

    Reviewed By: chad

    Subscribers: epriestley

    Differential Revision: https://secure.phabricator.com/D9508