commit 7c5b56c1c184feb6e112cadaea8dd68c7d3e24f1
Author: Martin Traverso <martint@fb.com>
Date:   Wed Mar 19 09:44:14 2014 -0700

    Simplify SortedRangeSet.Builder and add benchmark

    Collect ranges and do one pass at the end to merge adjacent and overlapping ranges.

    This improves performance for this class by about 6x for a synthetic benchmark with 10000 ranges

            Benchmark                                            Mode   Samples   Mean  Mean error   Units
    Before: c.f.p.s.BenchmarkSortedRangeSet.benchmarkBuilder     avgt        10  5.416       0.150   ms/op
    After:  c.f.p.s.BenchmarkSortedRangeSet.benchmarkBuilder     avgt        10  0.867       0.016   ms/op