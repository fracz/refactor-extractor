commit 078f95393529a0b1eac5b1ba931eb789d0a22969
Merge: 3bea01b dc6ee81
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Sep 28 15:36:39 2015 +0200

    feature #15356 [WebProfilerBundle] Profiler View Latest should preserve all the current query parameters (jbafford)

    This PR was merged into the 2.8 branch.

    Discussion
    ----------

    [WebProfilerBundle] Profiler View Latest should preserve all the current query parameters

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT
    | Doc PR        |

    This improves the View Latest link by preserving all of the current query parameters, which makes it more useful with the Logs panel, for example.

    Commits
    -------

    dc6ee81 Profiler View Latest should preserve all the current query parameters