commit 7696a91c311daa2153aaf48456684638e3af82fa
Author: Artem Prigoda <arteamon@gmail.com>
Date:   Wed Nov 2 13:05:55 2016 +0100

    Add support for configuring the blocking timeout for Jetty connectors (#1795)

    Jetty 9.3.2.v20150730 introduced a new option for the configuration of HTTP
    connection factories. The option is called `blockingTimeout` and allows to
    configure the timeout which applies to all blocking operations in addition
    to `idleTimeout`. The rationale behind it is to allow to set a timeout
    for blocking operations which perform I/O activity, but with a slow rate
    (for example serving a big static file). Such tasks won't exhaust the pool
    of worker threads which should improve the QoS for a Jetty server.

    See:

    https://dev.eclipse.org/mhonarc/lists/jetty-announce/msg00085.html
    https://bugs.eclipse.org/bugs/show_bug.cgi?id=472621