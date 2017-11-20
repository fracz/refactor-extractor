commit 182f7a5279864239901e68470d0ea5a7bcc5bbb3
Author: Ben Manes <ben.manes@gmail.com>
Date:   Sat Nov 5 21:03:50 2016 -0700

    Fix deadlock caused by unexpected OutOfMemoryError (fixes #130)

    For a small performance gain, scheduling the maintenance work is
    performed under a NonReentrantLock. This has a tiny effect, but is
    a win since scheduling might happen frequency under load. It only
    needed to be re-entrant when a caller-runs policy is used (so the
    task locks on the same thread as the one submitting under the lock)
    or if an exception is thrown and the maintenance work is performed
    directly. The ThreadPoolExecutor and like may throw a
    RejectedExecutionException, but ForkJoinPool will never throw. By
    default the non-reentrant lock is used, but if a custom executor
    is specified then a reentrant one is used since we can't trust
    the foreign code.

    However, an OutOfMemoryError may be thrown at any time and caused
    by unrelated code. So the assumption above was wrong, resulting in
    a deadlock. Despite the JVM being in an unpredictable state, its
    still a poor response.

    The test case covering this scenario was improved to run with the
    non-reentrant lock so the deadlock could be observed and corrected.

    In addition, removed the Gradle buildscan plugin due to it causing
    performance issues due to the large (95k) tests run per test task.