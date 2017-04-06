commit e8d7496b62f0b42843aeb9a6f86b6c004d9b56d2
Author: Georgios Kalpakas <kalpakas.g@gmail.com>
Date:   Wed Jul 27 19:35:48 2016 +0300

    perf($parse): improve performance of assignment expressions

    There was a ~5% improvement in the added `parsed-expressions-bp/assignment` benchmark (which only
    contains assignment expressions). In real-world applications, the time spent in assignment
    expressions will be a tiny fragment of the overall processing time, though.

    Closes #14957