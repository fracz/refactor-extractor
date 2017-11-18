commit 20540e4d1879c95589355159586c373d9fc9acde
Author: Artem Prigoda <aprigoda@bcc.ru>
Date:   Tue Jan 6 23:59:12 2015 +0300

    Warning about maximum pool size and unbounded queues

    Fix #575
    JDK ThreadPoolExecutor behaves a rather different than users can expect.

    A user expects that parameter 'maximumPoolSize' controls maximum amount
    of worker threads for processing tasks. When there is more tasks than
    amount of worker threads then the pool grows incrementally to that size.

    But actually ThreadPoolExecutor will grow the pool only if a queue is
    reached it's bound. If the queue is unbounded then it won't increase
    pool size beyond 'corePoolSize' limit. This seems rather awkward because
    queue type shouldn't be related with speed of it's processing.

    Anyway we should give a warning to users to improve their awareness
    of the situation.