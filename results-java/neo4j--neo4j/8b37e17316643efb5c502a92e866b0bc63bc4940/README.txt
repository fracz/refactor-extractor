commit 8b37e17316643efb5c502a92e866b0bc63bc4940
Author: Jacob Hansson <jacob@voltvoodoo.com>
Date:   Tue Oct 9 16:51:41 2012 +0200

    Refactored tx log error handling and handling of running out of disk errors:

    Now, we throw an appropriate exception if we run out of disk while writing the logical log, and that exception
    ends up getting handled by a more elaborate error handling mechanism in the tx manager.

    We also refactored the XaLogicalLog, such that the strategy for copying transactions over to a new log during rotation
    is handled by it's own class, covered with a unit test. This let us remove one of the breakpoint-based tests from the neo4j
    subproject.

    This commit will be followed by a second refactoring later this or next week where all usages of FileChannel.write will
    recieve the same protection that we added in the LogicalLog implementation.