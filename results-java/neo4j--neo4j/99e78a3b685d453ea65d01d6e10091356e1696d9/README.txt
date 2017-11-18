commit 99e78a3b685d453ea65d01d6e10091356e1696d9
Author: Tobias Lindaaker <tobias@thobe.org>
Date:   Wed Jul 17 12:59:45 2013 +0200

    Fix issues in the node->label store consistency checker.

    Since the checker reads full chains it needs to be able to gracefully handle
    records inconsistent in such a way that they form a cyclic chain, a (failing)
    test is added for this.
    It was discovered that exceptions in the checker were not always propagated,
    the code has been refactored to ensure that they are.

    This has been solved by:

    o Ensure that isStartRecord is written to the log
    o Load schema rules from consistency checker by forcing the records and ensuring that they are heavy
    o @Ignore tests for orphan checking of non-start records

    Also make tests resilient to GC behaviour:

    When using a spy to log all invocations on a checker to
    verify that different execution modes run in the same way,
    the calls to finalize() from the GC would be logged as well,
    since these are not triggered in a predictable way, we
    cannot reliably compare them, therefore we should filter
    them out instead.