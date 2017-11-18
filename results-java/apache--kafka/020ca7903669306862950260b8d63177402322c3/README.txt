commit 020ca7903669306862950260b8d63177402322c3
Author: Ben Stopford <benstopford@gmail.com>
Date:   Mon Apr 17 17:24:45 2017 -0700

    KAFKA-5036; Second part: Points 2 -> 5): Refactor caching of Latest Epoch

    This PR covers point (2) and point (5) from KAFKA-5036:

    **Commit 1:**
    2. Currently, we update the leader epoch in epochCache after log append in the follower but before log append in the leader. It would be more consistent to always do this after log append. This also avoids issues related to failure in log append.
    5. The constructor of LeaderEpochFileCache has the following:
    lock synchronized { ListBuffer(checkpoint.read(): _*) }
    But everywhere else uses a read or write lock. We should use consistent locking.
    This is a refactor to the way epochs are cached, replacing the code to cache the latest epoch in the LeaderEpochFileCache by reusing the cached value in Partition. There is no functional change.

    **Commit 2:**
    Adds an assert(epoch >=0) as epochs are written. Refactors tests so they never hit this assert.

    Author: Ben Stopford <benstopford@gmail.com>

    Reviewers: Jun Rao <junrao@gmail.com>

    Closes #2831 from benstopford/KAFKA-5036-part2-second-try