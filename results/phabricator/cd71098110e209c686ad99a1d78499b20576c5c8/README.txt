commit cd71098110e209c686ad99a1d78499b20576c5c8
Author: epriestley <git@epriestley.com>
Date:   Mon Sep 26 14:58:42 2011 -0700

    Detect commits by hash relationships

    Summary:
    When we discover a new commit and it has a known local commit or tree hash, mark
    it committed.

    This supports Mercurial and Git-Immutable workflows, and improves
    hybrid-Git-Mutable workflows and covers some cases where poeple just make
    mistakes or whatever.

    Test Plan: Parsed Mercurial, Git and SVN commits.

    Reviewers: Makinde

    Reviewed By: Makinde

    CC: aran, Makinde

    Differential Revision: 963