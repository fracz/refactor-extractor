commit 959bb16d0f6780c2691fd2ea7d2e724143ea8425
Author: epriestley <git@epriestley.com>
Date:   Sun Feb 21 10:52:19 2016 -0800

    Allow Almanac services to be searched by substring

    Summary: Ref T10246. Build an ngram index for Almanac services, and use it to support improved search.

    Test Plan: {F1121725}

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T10246

    Differential Revision: https://secure.phabricator.com/D15321