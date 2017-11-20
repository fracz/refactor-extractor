commit bfd12e4a86bfdc8bf49507476fa974dafc3ef59e
Author: Carl Mastrangelo <notcarl@google.com>
Date:   Fri Jul 22 11:16:42 2016 -0700

    benchmarks: improve benchmarks recording and shutdown

    The benchmarks today do not have a good way to record metrics with precision
    or shutdown safely when the benchmark is over.  This change alters the
    AbstractBenchmark class to return a latch that can be waited upon when ending
    the benchmark.

    Benchmarks also would accidentally request way too many messages from the
    server by calling request(1) explicitly in addition to the implicit one
    in the StreamObserver to Call adapter.  This change adds a few outstanding
    requests, but otherwise keeps the request count bounded.

    Additionally, benchmark calls would ignore errors, and just shutdown in such
    cases.  This changes them to log the error and just wait for the benchmark to
    complete.  In the successful case, the benchmark client notifies server by
    halfClosing (via onCompleted) where it previously did not.  It is also
    careful to only do this once.

    Lastly, Benchmarks have been changes to enable and disable recording at exact
    points in the benchmark method, rather than waiting for teardown to occur.
    Also, recording begins inside the recording method, not in Setup.  JMH may
    do other procressing before, between, and after iterations.