commit 1c1864be731a2bfce228dd5b2768419eaf1c8b01
Author: ZHANG Dapeng <zdapeng@google.com>
Date:   Tue Apr 4 18:19:41 2017 -0700

    netty: refactor NettyChannelBuilder keepalive API (#2874)

    To be in line with `NettyServerBuilder` APIs
    - Deprecated `enableKeepAlive(boolean enable)` and
    `enableKeepAlive(boolean enable, long keepAliveDelay, TimeUnit delayUnit, long keepAliveTimeout,
    TimeUnit timeoutUnit)`
    which never worked in v1.2

    - Added `keepAliveTime(long keepAliveTime, TimeUnit timeUnit)` and
    `keepAliveTimeout(long keepAliveTimeout, TimeUnit timeUnit)`