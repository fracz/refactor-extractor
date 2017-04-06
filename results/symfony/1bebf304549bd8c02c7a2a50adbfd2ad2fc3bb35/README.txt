commit 1bebf304549bd8c02c7a2a50adbfd2ad2fc3bb35
Merge: d189db7 ed8c1c0
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Mar 2 21:37:15 2012 +0100

    merged branch snc/profiler-tests (PR #3454)

    Commits
    -------

    ed8c1c0 Fixed AbstractProfilerStorageTest and some minor CS changes.
    1ac581e Overwrite the profile data if the token already exists like in the other implementations.
    198d406 Return profiler results sorted by time in descending order like in the other implementations.
    9d8e3f2 Refactored profiler storage tests to share some code.

    Discussion
    ----------

    [WIP] Refactored profiler tests including some storage fixes

    Bug fix: yes
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes

    While refactoring the tests I came across some inconsistencies. Two of them are already fixed in this PR.

    One thing left is the [MongoDbProfilerStorageTest::testCleanup()](https://github.com/snc/symfony/blob/9d8e3f2da4bea58316e32ef41d4e856a0cfdc872/tests/Symfony/Tests/Component/HttpKernel/Profiler/MongoDbProfilerStorageTest.php#L51) test which fails in all other storage implementations. The mongodb implementation uses the `time` value from the profiler data to clean up the storage while the others additionally save a `created_at` value which is then used. For me this `created_at` value does not make any sense and I would suggest to change the other implementations to use the `time` value for cleaning up. What do you think?

    ---------------------------------------------------------------------------

    by pulzarraider at 2012-02-27T06:55:06Z

    +1 for refactoring profiler tests, I will update my RedisProfilerStorage after your changes will be merged.

    ---------------------------------------------------------------------------

    by snc at 2012-02-28T20:05:12Z

    Any suggestions about the cleanup issue?