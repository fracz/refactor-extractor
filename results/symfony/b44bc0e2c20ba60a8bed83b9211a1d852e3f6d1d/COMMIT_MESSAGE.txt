commit b44bc0e2c20ba60a8bed83b9211a1d852e3f6d1d
Merge: 4705e6f 14e9f46
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Sep 18 15:11:39 2013 +0200

    merged branch fabpot/security-split (PR #9064)

    This PR was merged into the master branch.

    Discussion
    ----------

    [Security] Split the component into 3 sub-components Core, ACL, HTTP

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #9047, #8848
    | License       | MIT
    | Doc PR        | -

    The rationale behind this PR is to be able to use any of the sub components without requiring all the dependencies of the other sub components. Specifically, I'd like to use the core utils for an improved CSRF protection mechanism (#6554).

    Commits
    -------

    14e9f46 [Security] removed unneeded hard dependencies in Core
    5dbec8a [Security] fixed README files
    62bda79 [Security] copied the Resources/ directory to Core/Resources/
    7826781 [Security] Split the component into 3 sub-components Core, ACL, HTTP