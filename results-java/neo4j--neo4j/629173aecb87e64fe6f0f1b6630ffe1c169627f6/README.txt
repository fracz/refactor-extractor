commit 629173aecb87e64fe6f0f1b6630ffe1c169627f6
Author: Jonas Kalderstam <jonas@kalderstam.se>
Date:   Fri Jan 29 15:39:33 2016 +0100

    Improve robustness of HA-test infrastructure

    Makes operations such as fail, shutdown, and start, respect the implicit
    assumptions made about them, e.g. that once fail returns, the specific
    instance has been marked as failed and the cluster knows about it.

    These specific assumptions were false and made tests flaky. The
    multi-threaded nature here previously had no such guarantees and if a
    thread switch, garbage collection, or [insert favorite explanation here]
    occurred, the function might very well return before the instance had
    been failed/shutdown.

    As a requirement to this, the handling and detection of arbiter
    instances had to be improved as well.