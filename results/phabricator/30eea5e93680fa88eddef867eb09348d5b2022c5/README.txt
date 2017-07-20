commit 30eea5e93680fa88eddef867eb09348d5b2022c5
Author: epriestley <git@epriestley.com>
Date:   Fri Jan 23 08:36:27 2015 -0800

    Resolve an issue with Diffusion URI parsing ignoring some information

    Summary: Fixes T7011. Recent refactoring here caused us to begin ignoring URI parameters like `commit`. Most controllers take parameters as a `dblob`, which was still parsed properly.

    Test Plan:
      - Editing different commits actually edits the desired commits.
      - Browsed around some `dblob` pages and verified they still work properly.

    Reviewers: btrahan, chad

    Reviewed By: chad

    Subscribers: epriestley

    Maniphest Tasks: T7011

    Differential Revision: https://secure.phabricator.com/D11473