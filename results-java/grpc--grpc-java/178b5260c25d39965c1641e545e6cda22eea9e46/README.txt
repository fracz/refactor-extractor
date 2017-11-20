commit 178b5260c25d39965c1641e545e6cda22eea9e46
Author: Kun Zhang <zhangkun83@users.noreply.github.com>
Date:   Mon Dec 12 09:24:13 2016 -0800

    core: handle a race in DelayedClientTransport(2). (#2486)

    * Fork DelayedClientTransport into DelayedClientTransport2, and fix a race
    on it.  Consider the following sequence:

    1. Channel is created. Picker is initially null

    2. Channel has a new RPC. Because picker is null, the delayed transport
    is picked, but newStream() is not called yet.

    3. LoadBalancer updates a new picker to Channel. Channel runs
    reprocess() but does nothing because no pending stream is there.

    4. newStream() called on the delayed transport.

    In previous implementation, newStream() on step 4 will not see the
    picker. It will only use the next picker.

    After this change, delayed transport would save the latest picker and
    use it on newStream(), which is the desired behavior.

    Also deleted all the code that will not be used after the LB refactor.


    * Also fixed a bug: newStream() should always return a failing stream if it's shutdown. Previously it's not doing so if picker is not null.