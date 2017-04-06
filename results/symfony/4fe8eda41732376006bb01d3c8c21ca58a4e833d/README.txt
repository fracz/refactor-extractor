commit 4fe8eda41732376006bb01d3c8c21ca58a4e833d
Merge: 0e6465a bf174cf
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Oct 19 18:32:38 2014 +0200

    bug #12253 [FrameworkBundle] improve server commands feedback (xabbuh)

    This PR was merged into the 2.6-dev branch.

    Discussion
    ----------

    [FrameworkBundle] improve server commands feedback

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | kind of
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #12153
    | License       | MIT
    | Doc PR        |

    * display a message when `server:start` is executed and the PCNTL
      extension is not loaded
    * print instructions about how to terminate the `server:run` command

    Commits
    -------

    bf174cf [FrameworkBundle] improve server commands feedback