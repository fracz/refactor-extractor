commit b8fbe69bb88d08010e64fe8a4b74a89ff584df8a
Merge: 52915ed 972c4ca
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Dec 7 22:55:06 2015 +0100

    bug #16871 [FrameworkBundle] Disable built-in server commands when Process component is missing (gnugat, xabbuh)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [FrameworkBundle] Disable built-in server commands when Process component is missing

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT
    | Doc PR        |

    This also backports the improvement for the `suggest` section from #16650 to the `2.7` branch and improves it by also mentioning the other built-in server commands.

    Commits
    -------

    972c4ca disable server commands without Process component
    dd82b64 list all server command names in suggestion
    d18fb9b Suggested Process dependency