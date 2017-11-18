commit 43323c354115617b6f04eeb32b568a8092b9cab3
Author: Jason Tedor <jason@tedor.me>
Date:   Tue Nov 3 07:40:49 2015 -0500

    Fix bug in cat thread pool

    This commit fixes a bug in cat thread pool. This bug resulted from a
    refactoring of the handling of thread pool types. To get the previously
    displayed thread pool type from the ThreadPoolType object,
    ThreadPoolType#getType needs to be called.