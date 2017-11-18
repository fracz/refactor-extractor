commit 2d9bc101e86f76296b2a2f6cbffad313308223e6
Author: Alexey Kudravtsev <cdr@intellij.com>
Date:   Mon Sep 15 16:00:22 2014 +0400

    got rid of ourCheckCanceled;
    latency of ProgressManager.checkCanceled() improved, especially in case of multiple progresses.