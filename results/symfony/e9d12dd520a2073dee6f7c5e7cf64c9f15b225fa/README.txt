commit e9d12dd520a2073dee6f7c5e7cf64c9f15b225fa
Merge: c63bbe9 172fd63
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Dec 29 15:41:33 2013 +0100

    bug #9879 [Security] Fix ExceptionListener to catch correctly AccessDeniedException if is not first exception (fabpot)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [Security] Fix ExceptionListener to catch correctly AccessDeniedException if is not first exception

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #9544, #8467?, #9823
    | License       | MIT
    | Doc PR        |

    Same as #9823 but with some refactoring of the code and with some unit tests.

    When merging to 2.4, the unit tests can be simplified a bit.

    Commits
    -------

    172fd63 [Security] made code easier to understand, added some missing unit tests
    616b6c5 [Security] fixed error 500 instead of 403 if previous exception is provided to AccessDeniedException