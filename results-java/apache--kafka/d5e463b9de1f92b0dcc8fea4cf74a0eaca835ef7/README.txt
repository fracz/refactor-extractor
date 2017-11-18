commit d5e463b9de1f92b0dcc8fea4cf74a0eaca835ef7
Author: Guozhang Wang <wangguoz@gmail.com>
Date:   Fri Jun 23 18:01:41 2017 -0700

    KAFKA-4829: Improve log4j on Streams thread / task-level

    These are the following improvements I made:

    1. On stream thread level, INFO will be demonstrating `Completed xx tasks in yy ms` or `Completed rebalance with xx state in yy ms`,
    2. On Stream thread cache level, INFO on `Flushed xx records`.
    3. On Stream thread level, DEBUG on internal batched operations like `created xx tasks`, and TRACE on individual operation like `created x task`.
    4. Also using `isTraceEnabled` on the critical path to reduce overhead of creating `Object[]`.
    5. Minor cleanups in the code.

    Author: Guozhang Wang <wangguoz@gmail.com>

    Reviewers: Steven Schlansker, Nicolas Fouché, Kamal C, Ismael Juma, Bill Bejeck, Eno Thereska, Matthias J. Sax, Damian Guy

    Closes #3354 from guozhangwang/K4829-tasks-log4j