commit 6acf54f26bd2d7c0c32ab6a174448db2727a442b
Merge: 57310e9 b1c5a68
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Sep 28 07:37:20 2016 -0700

    bug #20066 [FrameworkBundle] fix yaml:lint when yaml is not installed along side framework-bundle (fabpot)

    This PR was merged into the 3.2-dev branch.

    Discussion
    ----------

    [FrameworkBundle] fix yaml:lint when yaml is not installed along side framework-bundle

    | Q             | A
    | ------------- | ---
    | Branch?       | master
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | n/a
    | License       | MIT
    | Doc PR        | n/a

    YAML is not an explicit dependency of FrameworkBundle. If it is not installed, the console is broken as the yaml:lint commands tries to extends the one in the YAML component. This bug only exists in master as this refactoring happened in 3.2

    Commits
    -------

    b1c5a68 [FrameworkBundle] fixed yaml:lint when yaml is not installed along side framwork-bundle