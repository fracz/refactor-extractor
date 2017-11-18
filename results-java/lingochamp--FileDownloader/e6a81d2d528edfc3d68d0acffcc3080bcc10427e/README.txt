commit e6a81d2d528edfc3d68d0acffcc3080bcc10427e
Author: Jacksgong <igzhenjie@gmail.com>
Date:   Mon Sep 5 10:53:01 2016 +0800

    refactor(attach-key): check whether the task has already attched to a key before add to global list, if not attach the default key to it manually, avoid the isolated task is assembled by a queue
    Closes #282