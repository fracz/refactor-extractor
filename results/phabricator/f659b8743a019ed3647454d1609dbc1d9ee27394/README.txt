commit f659b8743a019ed3647454d1609dbc1d9ee27394
Author: epriestley <git@epriestley.com>
Date:   Wed Aug 17 08:34:59 2016 -0700

    Fix Herald test adapter for commits

    Summary:
    Fixes T11488. I broke this in D16360, I think by doing a little extra refactoring after testing it.

    This code is very old, before commits always needed to have repositories attached in order to do policy checks.

    Modernize it by mostly just using the repository which is present on the Commit object, and using the existing edge cache.

    Test Plan: Ran a commit through the Herald test adapter.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T11488

    Differential Revision: https://secure.phabricator.com/D16413