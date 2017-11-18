commit 6183f1810040a4c3554ffadd16b830277f9ec9d0
Author: Chris Vest <mr.chrisvest@gmail.com>
Date:   Mon Dec 15 17:41:05 2014 +0100

    Produce fewer exceptions

    This fixes two hot exception producers that popped up during performance testing.
    This might slightly improve performance, but there's another benefit in that it removes two very noisy spots from the code, so we can get more interesting signal about rarer exceptions.

    The CollectUserAgentFilter is only a problem during performance testing, where the load generators don't provide a User-Agent header.
    Probably not an issue in production.

    The ResultIterator was implemented in a way that required non-local control flow through lambdas.
    This is implemented in Scala with trace-less exceptions.
    Changing the logic around to no longer do an early return removes the need for this control-flow-by-exception.