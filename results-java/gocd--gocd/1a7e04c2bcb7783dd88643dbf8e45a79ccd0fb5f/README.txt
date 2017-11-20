commit 1a7e04c2bcb7783dd88643dbf8e45a79ccd0fb5f
Author: Xiao Li <swing1979@gmail.com>
Date:   Fri Jan 8 15:56:05 2016 -0800

    #1098 [performance] Agent improvements:
    1. handle canceling job properly, log running job details if we can not cancel it in 30 seconds.
    2. try cancel running job if agent got assigned new work, which may happen due to server rescheduled job and agent reconnected with wrong status;
    3. fix tests broke by previous commit