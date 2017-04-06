commit 73149ae819a976ebe658ba6b354507687fa408f1
Merge: 3d63748 7887a46
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Dec 3 10:28:04 2014 +0100

    bug #12823 [DependencyInjection] fix PhpDumper (nicolas-grekas)

    This PR was merged into the 2.6 branch.

    Discussion
    ----------

    [DependencyInjection] fix PhpDumper

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #12815
    | License       | MIT
    | Doc PR        | none

    Commits
    -------

    7887a46 [DependencyInjection] keep some of the reverted perf optim
    2f0b355 Revert "minor #10241 [DependencyInjection] made some perf improvements (fabpot)"