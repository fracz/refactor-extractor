commit 8ee2a6f9888e2e37fbffc9eac92a1b057271a451
Author: vrana <jakubv@fb.com>
Date:   Fri Aug 17 12:46:26 2012 -0700

    Explicitly load assets in revision list

    Summary:
    Rendering method shouldn't load data.
    The view probably shouldn't load data either because it is a job for component (object that both loads data and displays them) but we don't have that concept in Phabricator.
    This at least improves the architecture a little bit.

    Test Plan: /differential/

    Reviewers: epriestley

    Reviewed By: epriestley

    CC: alanh, aran, Korvin

    Differential Revision: https://secure.phabricator.com/D3325