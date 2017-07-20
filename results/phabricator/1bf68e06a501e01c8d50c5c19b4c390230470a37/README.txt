commit 1bf68e06a501e01c8d50c5c19b4c390230470a37
Author: epriestley <git@epriestley.com>
Date:   Wed May 9 17:28:57 2012 -0700

    Improve Diffusion error messages and UI for partially imported repositories

    Summary:
      - When you have an un-cloned repository, we currently throw random-looking Git/Hg exception. Instead, throw a useful error.
      - When you have a cloned but undiscovered repository, we show no commits. This is crazy confusing. Instead, show commits as "importing...".
      - Fix some warnings and errors for empty path table cases, etc.

    Test Plan:
      - Wiped database.
      - Added Mercurial repo without running daemons. Viewed in Diffusion, got a good exception.
      - Pulled Mercurial repo without discovering it. Got "Importing...".
      - Discovered Mercurial repo without parsing it. Got "Importing..." plus date information.
      - Parsed Mercurial repo, got everything working properly.
      - Added Git repo without running daemons, did all the stuff above, same results.
      - This doesn't improve SVN much but that's a trickier case since we don't actually make SVN calls and rely only on the parse state.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T776

    Differential Revision: https://secure.phabricator.com/D2439