commit e0dbc575219569f23c09daa575038252fb4587e0
Author: epriestley <git@epriestley.com>
Date:   Fri Feb 1 10:52:07 2013 -0800

    Stabilize scroll position as diffs load

    Summary:
    Try to lock the screen to whatever the user is looking at as we load changesets.

    Notably, this improves the use case of taking a known action on a diff. Currently, you have to wait for everything to load or the comments keep getting scrolled down. After this change, the comments stay in the same place on screen.

    Test Plan:
    Raised the autoload changeset limit from 100 to 1000, looked at a 220 changeset diff.

      - Reloaded it while scrolled at the top; normal behavior (no scrolling).
      - Reloaded it, scrolled to the bottom. Comment area now stable.
      - Reloaded it, kind of scrolled around the middle? Behavior seemed stable/reasonable. This one is kind of heursitic so it's hard to say I'm getting it totally right or not, but it's less important than the "bottom" case.

    Reviewers: vrana, btrahan, chad, dctrwatson

    Reviewed By: btrahan

    CC: aran

    Differential Revision: https://secure.phabricator.com/D4774