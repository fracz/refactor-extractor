commit f8fb8a98b2f9d797483a020cee3ec6ad2e82c366
Merge: 79a5fdd 88e3314
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Dec 4 11:25:57 2014 +0100

    minor #12796 [Console] improve deprecation warning triggers (xabbuh)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [Console] improve deprecation warning triggers

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #12771, #12791
    | License       | MIT
    | Doc PR        |

    Since the default helper set of the Console `Application` relies on
    the `DialogHelper`, the `ProgressHelper` and the `TableHelper`, there
    must be a way to not always trigger `E_USER_DEPRECATION` errors when
    one of these helper is used.

    Commits
    -------

    88e3314 [Console] improve deprecation warning triggers