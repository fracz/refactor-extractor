commit eb2d65bb0c216737d36bae03480e55b86b633b84
Author: Xudong Ma <simonma@google.com>
Date:   Tue Mar 31 19:35:44 2015 +0800

    Disable Nagle's algorithm, this was exposed by our internal micro benchmark, the streamingInputCall() method happened to have the write-write-read pattern which added 40ms extra delay.

    According to our micro benchmark, this change improves the performance, see https://screenshot.googleplex.com/cfxjPGxkgF.png for the previous results, there were 4 out of 6 scenarios had higher latency than netty transport.

    https://screenshot.googleplex.com/Rq92tYMvBE.png shows the results after this change, the latencies in all scenarios are less than netty transport.

    This commit fixes #60.