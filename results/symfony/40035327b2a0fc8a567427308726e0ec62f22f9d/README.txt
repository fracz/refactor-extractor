commit 40035327b2a0fc8a567427308726e0ec62f22f9d
Merge: a0a97d7 132a4e4
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Jan 25 18:08:12 2016 +0100

    bug #15272 [FrameworkBundle] Fix template location for PHP templates (jakzal)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [FrameworkBundle] Fix template location for PHP templates

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #14804
    | License       | MIT
    | Doc PR        | -

    - [x] improve the test to cover logical path & filesystem path
    - [x] Add a new test case and fix the path to the template

    As the first commit only enchanced the test, and the second commit fixed the bug, it's best to review them seperately.

    Commits
    -------

    132a4e4 [FrameworkBundle] Fix template location for PHP templates
    cd42e2d [FrameworkBundle] Add path verification to the template parsing test cases