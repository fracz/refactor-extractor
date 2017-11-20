commit 7fd2a04c202fc6b9242408777318660943c2a438
Author: Xiao Li <swing1979@gmail.com>
Date:   Fri Jan 8 17:35:59 2016 -0800

    #1098 [performance] Agent improvements:
    1. change JobRunner attributes to use volatile keyword instead of AtomicBoolean which is too much for our case
    2. use an Executor thread pool for processing server messages to avoid blocking by assign work message