commit e1d1832443291bb5f05e9a7a706998873ef35d1b
Merge: 8487ef2 264c37a
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Oct 20 11:15:22 2014 +0200

    minor #12257 [FrameworkBundle] improve server:run feedback (xabbuh)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [FrameworkBundle] improve server:run feedback

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT
    | Doc PR        |

    In #12253, improved feedback was added to the `server:run` command
    instructing the user how to terminate the built-in web server. This
    is hereby backported to the `2.3` branch.

    Commits
    -------

    264c37a [FrameworkBundle] improve server:run feedback