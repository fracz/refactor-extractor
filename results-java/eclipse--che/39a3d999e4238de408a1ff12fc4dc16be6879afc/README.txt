commit 39a3d999e4238de408a1ff12fc4dc16be6879afc
Author: David Festal <dfestal@redhat.com>
Date:   Mon Oct 30 16:23:21 2017 +0100

    Fix for #7058 ... (#7071)

    * Fix for #7058 ...

    ... Make the new `always-external-custom` server evaluation strategy
    also support `https`. The previous PR #7004 to fix issue #6953 was not
    complete and didn't support the secured use-case correctly.

    * added the requested refactoring + unit tests

    Signed-off-by: David Festal <dfestal@redhat.com>