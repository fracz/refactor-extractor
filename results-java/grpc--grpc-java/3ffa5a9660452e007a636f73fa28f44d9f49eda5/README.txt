commit 3ffa5a9660452e007a636f73fa28f44d9f49eda5
Author: ZHANG Dapeng <zdapeng@google.com>
Date:   Wed Mar 22 10:26:45 2017 -0700

    Okhttp: keepAlivedManager#onTransportShutdown moved from shutdown to stopIfNecessary and refactored

    `keepAlivedManager#onTransportshutdown` should not be called in `transport.shutdown()` because it is possible that there are still open RPC streams, and maybe inactive, so keepalive is still needed.