commit 57278aa82da5dc9d040eb3dcf4a182e0731a621b
Author: Guozhang Wang <wangguoz@gmail.com>
Date:   Wed Mar 22 14:33:34 2017 -0700

    MINOR: Improvements on Streams log4j

    1. add thread id as prefix in state directory classes; also added logs for lock activities.
    2. add logging for task creation / suspension.
    3. add more information in rebalance listener logging.
    4. add restored number of records into changlog reader.

    Author: Guozhang Wang <wangguoz@gmail.com>

    Reviewers: Eno Thereska, Damian Guy, Ewen Cheslack-Postava

    Closes #2702 from guozhangwang/KMinor-streams-task-creation-log4j-improvements