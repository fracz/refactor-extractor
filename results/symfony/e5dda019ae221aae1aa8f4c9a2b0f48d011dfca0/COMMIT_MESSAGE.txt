commit e5dda019ae221aae1aa8f4c9a2b0f48d011dfca0
Merge: 800232c 99d1741
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Dec 7 22:51:02 2015 +0100

    bug #16870 [FrameworkBundle] Disable the server:run command when Process component is missing (gnugat, xabbuh)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [FrameworkBundle] Disable the server:run command when Process component is missing

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

    This also backports the improvement for the `suggest` section from #16650 to the `2.3` branch.

    Commits
    -------

    99d1741 disable server:run cmd without Process component
    604174c Suggested Process dependency