commit b74bb0019eb5569f308122f250ed04bfb6317ac3
Author: epriestley <git@epriestley.com>
Date:   Tue Apr 21 05:36:20 2015 -0700

    Filter nonvisible inlines out of changeset inline result set

    Summary:
    Ref T7447. After compiling inlines which will appear on the changeset, remove inlines which

    Later stages remove these anyway, so it doesn't change anything to keep them around, but we can filter them out here cheaply.

    This will also let us drive the Differential timeline view with the same logic a few diffs from now, to improve how it renders inlines. Generalize things a little bit.

    Test Plan:
      - Made a comment on the left of diff 1.
      - Made diff 2.
      - Viewed diff 2 vs diff 1.
      - Verified old-left comment was filtered out by the new loop.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T7447

    Differential Revision: https://secure.phabricator.com/D12488