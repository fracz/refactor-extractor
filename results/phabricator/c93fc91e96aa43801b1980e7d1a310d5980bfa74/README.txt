commit c93fc91e96aa43801b1980e7d1a310d5980bfa74
Author: epriestley <git@epriestley.com>
Date:   Mon Dec 19 09:06:09 2011 -0800

    Update Javelin; improve package definitions

    Summary:
      - Update Javelin to HEAD -- this doesn't pick up anything in particular, but
    lets us smoke test some stuff like {D1217}.
      - Do a little more packaging since we've picked up a handful of 10-line
    behaviors and such for various UI tweaks.

    Test Plan:
      - Generally, this should be very low-risk.
      - Browed Maniphest, Differential, Diffusion and tried to hit all the JS
    interactions.
      - Looked over the Javelin changes we're pulling in to see if I forgot
    anything. The only API change I caught was removal of "JX.defer()", but that was
    already cleared in Phabricator in D803.

    Reviewers: aran, btrahan, jungejason

    Reviewed By: aran

    CC: aran

    Differential Revision: 1240