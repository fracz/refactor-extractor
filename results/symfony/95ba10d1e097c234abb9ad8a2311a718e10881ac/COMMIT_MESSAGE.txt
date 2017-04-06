commit 95ba10d1e097c234abb9ad8a2311a718e10881ac
Merge: 7e657b8 302a19d
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Dec 13 09:29:27 2016 +0100

    feature #20866 [Console] Improve markdown format (ro0NL)

    This PR was merged into the 3.3-dev branch.

    Discussion
    ----------

    [Console] Improve markdown format

    | Q             | A
    | ------------- | ---
    | Branch?       | "master"
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | not sure?
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | comma-separated list of tickets fixed by the PR, if any
    | License       | MIT
    | Doc PR        | reference to the documentation PR, if any

    This improves the markdown description for a console application. To make the ouput read more friendly and intuitively (less bloated IMHO).

    Before:

    Markdown files in https://github.com/symfony/symfony/tree/master/src/Symfony/Component/Console/Tests/Fixtures

    After:

    Markdown files in https://github.com/ro0NL/symfony/tree/console/markdown/src/Symfony/Component/Console/Tests/Fixtures

    Commits
    -------

    302a19d [Console] Improve markdown format