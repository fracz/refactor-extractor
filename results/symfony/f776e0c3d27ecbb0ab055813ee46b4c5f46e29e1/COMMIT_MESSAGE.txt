commit f776e0c3d27ecbb0ab055813ee46b4c5f46e29e1
Merge: 71e303b c3cce5c
Author: Bernhard Schussek <bschussek@gmail.com>
Date:   Mon Sep 15 22:32:55 2014 +0200

    bug #11907 [Intl] Improved bundle reader implementations (webmozart)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [Intl] Improved bundle reader implementations

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    This PR extracts bundle reader improvements from #9206.

    The code is internal and used for resource bundle generation only, so I did not care about BC too much.

    Commits
    -------

    c3cce5c [Intl] Improved bundle reader implementations