commit 83fd84c0cd78f03713081e42c23ece52c8a7b827
Author: Charles Munger <charleslmunger@gmail.com>
Date:   Mon May 2 22:38:33 2016 -0700

    Fix exceptions thrown by ShadowSQLiteConnection (#2434)

    In addition to making the exceptions thrown match those thrown from
    android, this CL improves the stack traces - all exceptions thrown
    include the ExecutionException in them, which shows the real invocation
    site of the command throwing the exception, not just the stack trace of
    ShadowSQLiteConnection’s internal executor.

    Also, interrupting the thread making a call to the database no longer
    results in an exception.