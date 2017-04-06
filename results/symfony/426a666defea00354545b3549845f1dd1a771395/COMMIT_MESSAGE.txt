commit 426a666defea00354545b3549845f1dd1a771395
Merge: 05933f3 b14057c
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Mar 22 16:02:33 2017 -0700

    minor #22043 Refactor stale-while-revalidate code in HttpCache, add a (first?) test for it (mpdude)

    This PR was squashed before being merged into the 3.3-dev branch (closes #22043).

    Discussion
    ----------

    Refactor stale-while-revalidate code in HttpCache, add a (first?) test for it

    | Q             | A
    | ------------- | ---
    | Branch?       | 2.8
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT
    | Doc PR        |

    I came up with this while trying to hunt a production bug related to handling of stale cache entries under the condition of a busy backend (also see #22033).

    It's just a refactoring to make the code more readable plus a new test.

    Commits
    -------

    b14057c88a Refactor stale-while-revalidate code in HttpCache, add a (first?) test for it