commit 3263fe17bb44fa9cddd72fe17e4d4401fe16fd77
Merge: fea27c1 a59e5e4
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Mar 6 20:43:20 2013 +0100

    merged branch gajdaw/finder_unified_tests (PR #7197)

    This PR was merged into the 2.2 branch.

    Commits
    -------

    a59e5e4 [Finder] Unified tests

    Discussion
    ----------

    [Finder] Unified tests

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    Tests for `Finder` are very difficult to read because they contain calls to:

        sys_get_temp_dir() . '/symfony2_finder/'

    This improved version simplifies adding and removing new dirs and files.