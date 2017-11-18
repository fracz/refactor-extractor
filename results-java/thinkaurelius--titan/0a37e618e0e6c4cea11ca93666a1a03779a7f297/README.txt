commit 0a37e618e0e6c4cea11ca93666a1a03779a7f297
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Mon Jun 23 12:29:58 2014 -0400

    Performance test refactoring

    This commit adds a new test, TitanWritePerformanceTest, which tests
    vertex, edge, and property insertion on an empty graph with and
    without batch loading enabled.  The code is descended from
    TitanGraphPerformanceTest last seen in
    7d69240e324cb5c2bef71a25dc88fbead1697308, but it's been refactored to
    use BatchGraph and helper methods in TitanGraphTestCommon for schema
    creation, and to de-noisify the edge insertion pattern.

    This commit removes several performance test classes:

    * the two-method "PerformanceTest" and the TitanBenchmarkSuite class
      that used it.  There were no external references to these classes;
      the only reference appeared to be a block of commented code in
      TitanBlueprintsTest, which was also removed.

    * SpeedComparisonBenchmark, which seems mostly subsumed by other test
      classes (except possibly the VertexList retrieval)

    * KeyColumnValueStorePerformance -- this is a good idea, but it
      appears to have been barely started and never really finished.  The
      only subclass in the source tree is for the InMemory backend.
      Deleting it for now since the performance data it produces are not
      useful, but maybe we will restore and flesh it out later.

    SerializerTest contained a combination of correctness-focused and
    performance-focused tests.  I extracted common logic into
    SerializerTestCommon, then moved the performance tests to
    SerializerSpeedTest (which also bears the PerformanceTests JUnit
    category).  The correctness-focused tests are still in SerializerTest.

    This commit adds a new enabled-by-default JUnitBenchmarks reporter
    that writes a CSV file with one line per test method.  The reporter is
    internally synchronized but it isn't safe for use by concurrent
    processes (e.g. multiple surefire test forks).  The file path is
    target/jub.csv.  The output includes an initial header row.