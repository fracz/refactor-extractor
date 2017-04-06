commit d01a10651be13ee84ebc18a8512fb2b3328b0f2a
Merge: 2710a88 72940d7
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Mar 21 09:49:16 2016 -0700

    bug #18224 [PropertyAccess] Remove most ref mismatches to improve perf (nicolas-grekas)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [PropertyAccess] Remove most ref mismatches to improve perf

    | Q             | A
    | ------------- | ---
    | Branch?       | 2.3
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | no
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    This PR is for PHP5 where ref mismatches is a perf pain: it removes all ref mismatches along the "getValue" path, and keeps only the required ones on the "setValue" path.

    Commits
    -------

    72940d7 [PropertyAccess] Remove most ref mismatches to improve perf