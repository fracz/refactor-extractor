commit d8d79081097d26ede6599ef7f239707c294ef9c1
Author: Louis Ryan <lryan@google.com>
Date:   Tue Apr 28 10:21:35 2015 -0700

    Avoid flushing CreateStreamCommand for calls where the client is known to send a payload immediately afterward.
    Added simple JMH benchmark for Netty so improvements can be measured