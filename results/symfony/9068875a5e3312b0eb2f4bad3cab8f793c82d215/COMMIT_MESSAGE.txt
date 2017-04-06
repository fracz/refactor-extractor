commit 9068875a5e3312b0eb2f4bad3cab8f793c82d215
Merge: e4ff8ae 6dba229
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Mar 27 08:52:42 2014 +0100

    bug #10540 [HttpKernel] made parsing controllers more robust (fabpot)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [HttpKernel] made parsing controllers more robust

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #10465
    | License       | MIT
    | Doc PR        | n/a

    Under some circumstances (like passing an object without an __invoke method), the Controller resolver would lead to a PHP fatal. This PR fixes that, improved error messages, and refactored the unit tests.

    Commits
    -------

    6dba229 made parsing controllers more robust