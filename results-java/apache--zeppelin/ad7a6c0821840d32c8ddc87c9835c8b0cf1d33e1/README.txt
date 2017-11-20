commit ad7a6c0821840d32c8ddc87c9835c8b0cf1d33e1
Author: Prabhjyot Singh <prabhjyotsingh@gmail.com>
Date:   Fri Dec 18 12:35:48 2015 +0530

    Add firebug and firepath for debugging

    Hi,

    I've refactored some of the selenium related test case, and added firebug and firepath for debugging so that if CI is failing on travis it can be debugged on local.

    Also I feel that we should delete notebook once selenium test case is finished.

    Author: Prabhjyot Singh <prabhjyotsingh@gmail.com>

    Closes #490 from prabhjyotsingh/addFirebug and squashes the following commits:

    1cc00e0 [Prabhjyot Singh] Merge remote-tracking branch 'remote/master' into addFirebug
    6af0aa6 [Prabhjyot Singh] resolving merge issue
    28f5087 [Prabhjyot Singh] Fixing CI failure
    31e330c [Prabhjyot Singh] remove firebug binaries
    3e8613e [Prabhjyot Singh] Download firebug binaries insted of keeping it in resource
    4e0e33c [Prabhjyot Singh] refactor add firebug and firepath for debugging delete notebook once test is finished