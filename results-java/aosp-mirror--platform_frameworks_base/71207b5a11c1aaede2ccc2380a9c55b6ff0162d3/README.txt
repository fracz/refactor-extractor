commit 71207b5a11c1aaede2ccc2380a9c55b6ff0162d3
Author: John Reck <jreck@google.com>
Date:   Wed Sep 28 13:28:09 2016 -0700

    Switch Parcel to FastNative

    Also fixes ParcelBenchmark to have bounded
    memory usage to avoid OOM'ing during runs

    Test: refactor, no behavior change

    ParcelBenchmark results from bullhead
    Before:
          timeReadByte 74.43ns
          timeReadInt 74.49ns
          timeReadLong 74.13ns
          timeWriteByte 81.81ns
          timeWriteInt 82.09ns
          timeWriteLong 81.84ns

    After:
          timeReadByte 47.08ns
          timeReadInt 48.38ns
          timeReadLong 48.16ns
          timeWriteByte 55.90ns
          timeWriteInt 55.85ns
          timeWriteLong 56.58ns

    Change-Id: I61b823d1d2beb86e00c196abd4dce65efa8fa7f0