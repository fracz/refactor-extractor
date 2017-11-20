commit 602473d78611847b580accd3ce293d2b24140745
Author: buchgr <jakob.buchgraber@tum.de>
Date:   Wed Nov 11 12:53:08 2015 +0100

    Add directExecutor() to Channel and Server Builders. Fixes #368.

    When using a direct executor we don't need to wrap calls in a
    serializing executor and can thus also avoid the overhead that
    comes with it.

    Benchmarks show that throughput can be improved substantially.
    On my MBP I get a 24% improvement in throughput with also
    significantly better latency throughout all percentiles.

    (running qps_client and qps_server with --address=localhost:1234 --directexecutor)

    === BEFORE ===
    Channels:                       4
    Outstanding RPCs per Channel:   10
    Server Payload Size:            0
    Client Payload Size:            0
    50%ile Latency (in micros):     452
    90%ile Latency (in micros):     600
    95%ile Latency (in micros):     726
    99%ile Latency (in micros):     1314
    99.9%ile Latency (in micros):   5663
    Maximum Latency (in micros):    136447
    QPS:                            78498

    === AFTER ===
    Channels:                       4
    Outstanding RPCs per Channel:   10
    Server Payload Size:            0
    Client Payload Size:            0
    50%ile Latency (in micros):     399
    90%ile Latency (in micros):     429
    95%ile Latency (in micros):     453
    99%ile Latency (in micros):     650
    99.9%ile Latency (in micros):   1265
    Maximum Latency (in micros):    33855
    QPS:                            97552