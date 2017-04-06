commit 1339c11e36d9b9d23b26b98e90b523ab99901cfe
Author: Lucas Galfaso <lgalfaso@gmail.com>
Date:   Sat Aug 2 04:39:40 2014 +0200

    refactor($q): make $q Promises A+ v1.1 compilant

    The Promises A+ 1.1 spec introduces new constraints that would cause $q to fail,
    particularly specs 2.3.1 and 2.3.3.

    Newly satisfied requirements:

     * "then" functions that return the same fulfilled/rejected promise
            will fail with a TypeError
     * Support for edge cases where "then" is a value other than function

    Full 1.1 spec: https://github.com/promises-aplus/promises-spec/tree/1.1.0

    This commit also modifies the adapter to use "resolve" method instead of "fulfill"