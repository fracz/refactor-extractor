commit 668dac722a4f80911dfb02f6cb6ef73f1b0f42e1
Author: Michael McCandless <mike@elastic.co>
Date:   Tue Aug 23 06:37:38 2016 -0400

    Don't suppress AlreadyClosedException (#19975)

    Catching and suppressing AlreadyClosedException from Lucene is dangerous because it can mean there is a bug in ES since ES should normally guard against invoking Lucene classes after they were closed.

    I reviewed the cases where we catch AlreadyClosedException from Lucene and removed the ones that I believe are not needed, or improved comments explaining why ACE is OK in that case.

    I think (@s1monw can you confirm?) that holding the engine's readLock means IW will not be closed, except if disaster strikes (failEngine) at which point I think it's fine to see the original ACE in the logs?

    Closes #19861