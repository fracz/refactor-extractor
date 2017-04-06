commit 9e57ce0c7af742d15223b9d2aa1f9aed5792d007
Author: Igor Minar <igor@angularjs.org>
Date:   Mon Feb 11 21:58:00 2013 -0800

    revert: refactor(angular.copy): use slice(0) to clone arrays

    This reverts commit 28273b7f1ef52e46d5bc23c41bc7a1492cf23014o.

    slice(0) doesn't perform deep copy of the array so its unsuitable
    for our purposes.