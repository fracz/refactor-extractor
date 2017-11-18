commit f7a51d1ee2bf1e372db480e15b3cc38dc668b8da
Author: Cedric Champeau <cedric@gradle.com>
Date:   Thu Dec 22 11:18:52 2016 +0100

    Back incremental compiler caches with in-memory caches, avoiding a lot of disk (de)serialization

    This dramatically improves the performance of the incremental compiler in large builds (from seconds of analysis to ms).