commit 43f74ef41f5918ad60ba65e6ed68b1b7fc4f5978
Merge: de0bd91 f885b9b
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Mar 9 16:14:43 2015 +0100

    minor #13845 [travis] Test with local components instead of waiting for the subtree-splitter when possible (nicolas-grekas)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [travis] Test with local components instead of waiting for the subtree-splitter when possible

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

    Should be ready - a first step that allows testing PR with the proposed patch. Works only when deps are resolved to the same branch. But this opens the way for further improvements.

    Commits
    -------

    f885b9b Test with local components instead of waiting for the subtree-splitter when possible