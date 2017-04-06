commit 0af3d19c3855b18a8742b82b1d1ea7fe8535c412
Merge: d56cc4b 557dfaa
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Dec 16 17:42:19 2013 +0100

    bug #9601 [Routing] Remove usage of deprecated _scheme requirement (Danez)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [Routing] Remove usage of deprecated _scheme requirement

    **This is exact the same commit as it was in #9585, which was not merged due to my fault. Sorry for the noise.**

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #8898, #8176
    | License       | MIT
    | Doc PR        |

    I removed all usages of the deprecated _scheme requirement inside the Routing Component.
    Most parts were pretty easy and after multiple refactorings I came up with the solution to have a Route::hasScheme() method and check against this method.

    I also checked for performance and after trying in_array, arra_flip+isset and foreach, the last one was clearly the winner.
    https://gist.github.com/Danez/7609898#file-test_performance-php

    I also adjusted all tests that test '_scheme' to also check the new schemes-requirement.

    Commits
    -------

    557dfaa Remove usage of deprecated _scheme in Routing Component