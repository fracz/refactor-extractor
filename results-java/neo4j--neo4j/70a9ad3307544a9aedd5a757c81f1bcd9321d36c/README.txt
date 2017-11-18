commit 70a9ad3307544a9aedd5a757c81f1bcd9321d36c
Author: Chris Vest <mr.chrisvest@gmail.com>
Date:   Mon Sep 26 13:48:10 2016 +0200

    Move the WorkSynd done-marking out of the critical section

    This allows the next batch to start processing while the current batch is waking up threads that submitted work to it.

    This is incompatible with successor-based unparking, wherein a specific successor was chosen among the parked work submitters.
    The job of the successor was then to unpark all of the other parked work submitters in the batch.
    However, now that the done-marking has moved out of the critical section, it was then possible for threads to notice that their work was completed before they parked.
    Thus, they would never wake up as successors and therefor never take the job of unparking the other waiting threads in the batch.
    This was possibly a bug in the previous implementation as well.

    Now, the thread that got the lock, and combined and performed the work, is now also responsible for unparking all threads in the batch.
    Since this is no longer done while holding the WorkSync lock, it is perfectly okay that this thread spends the extra time to do this work.
    The reason we introduced successor-based unparking was to reduce the amount of time the work-doing thread spent holding the WorkSync lock.

    Quick testing shows that this also improves the performance by a few percent.