commit 907f1a3dee024ebf26e5a8fbfd6cd19437f7473f
Author: epriestley <git@epriestley.com>
Date:   Wed May 9 10:01:53 2012 -0700

    Raise a "you should upgrade your storage" note for a missing database

    Summary: We raise an improved exception for missing tables/columns, but not databases.

    Test Plan: Hit a "no such database error", got a better error message pointing me at storage upgrades.

    Reviewers: btrahan, jungejason

    Reviewed By: btrahan

    CC: aran

    Differential Revision: https://secure.phabricator.com/D2429