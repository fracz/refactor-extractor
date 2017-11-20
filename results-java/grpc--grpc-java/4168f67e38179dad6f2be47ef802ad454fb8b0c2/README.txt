commit 4168f67e38179dad6f2be47ef802ad454fb8b0c2
Author: Eric Anderson <ejona@google.com>
Date:   Tue Aug 4 16:37:00 2015 -0700

    Optimize blocking calls to avoid app thread pool

    This reduces the necessary number of threads in the application executor
    and provides a small improvement in latency (~15μs, which is normally in
    the noise, but would be a 5% improvement).

    Benchmark                         (direct)  (transport)  Mode  Cnt       Score        Error  Units
    Before:
    TransportBenchmark.unaryCall1024      true    INPROCESS  avgt   10    1566.168 ±     13.677  ns/op
    TransportBenchmark.unaryCall1024     false    INPROCESS  avgt   10   35769.532 ±   2358.967  ns/op
    After:
    TransportBenchmark.unaryCall1024      true    INPROCESS  avgt   10    1813.778 ±     19.995  ns/op
    TransportBenchmark.unaryCall1024     false    INPROCESS  avgt   10   18568.223 ±   1679.306  ns/op

    The benchmark results are exactly what we would expect, assuming that
    half of the benefit of direct is on server and half on client:
    1566 + (35769 - 1566) / 2 = 18668 ns --vs-- 18568 ns

    It is expected that direct=true would get worse, because
    SerializingExecutor is now used instead of
    SerializeReentrantCallsDirectExecutor plus the additional cost of
    ThreadlessExecutor.

    In the future we could try to detect the ThreadlessExecutor and ellide
    Serializ*Executor completely (as is possible for any single-threaded
    executor). We could also optimize the queue used in ThreadlessExecutor
    to be single-producer, single-consumer. I don't expect to do those
    optimizations soon, however.