commit 70e31082827772fc39cbecc713f9bbd0d4de1647
Author: Charles Allen <charles@allen-net.com>
Date:   Tue Nov 18 22:07:18 2014 -0800

    Multiple speed improvements revolving around topN with HLL

    Change serializer / deserializer for HyperLogLog
    * Changed DirectDruidClient's InputStream handling. Is now ~10% faster for data heavy queries, and has lower variance in execution speed.
    * Changed HLL Collector's toByteStream() method to be better optimized for small values. Is notably faster for small result quantities which fall into the sparse HLL bucket codepath.
      * No change for dense HLL which just uses a direct bytestream of the underlying byte data.

    TopNNumericResultBuilder semi-aggressive loop unrolling for metricVals

    Benchmark for HLL for sparse packing (small HLL bucket population):
    HyperLogLogSerdeBenchmarkTest.benchmarkToByteBuffer[0]: [measured 100000 out of 100100 rounds, threads: 1 (sequential)]
     round: 0.00 [+- 0.00], round.block: 0.00 [+- 0.00], round.gc: 0.00 [+- 0.00], GC.calls: 216, GC.time: 0.42, time.total: 15.96, time.warmup: 0.22, time.bench: 15.74
    HyperLogLogSerdeBenchmarkTest.benchmarkToByteBuffer[1]: [measured 100000 out of 100100 rounds, threads: 1 (sequential)]
     round: 0.00 [+- 0.00], round.block: 0.00 [+- 0.00], round.gc: 0.00 [+- 0.00], GC.calls: 217, GC.time: 0.45, time.total: 13.87, time.warmup: 0.02, time.bench: 13.85
    HyperLogLogSerdeBenchmarkTest.benchmarkToByteBuffer[2]: [measured 100000 out of 100100 rounds, threads: 1 (sequential)]
     round: 0.00 [+- 0.00], round.block: 0.00 [+- 0.00], round.gc: 0.00 [+- 0.00], GC.calls: 55, GC.time: 0.16, time.total: 4.13, time.warmup: 0.00, time.bench: 4.12
    HyperLogLogSerdeBenchmarkTest.benchmarkToByteBuffer[3]: [measured 100000 out of 100100 rounds, threads: 1 (sequential)]
     round: 0.00 [+- 0.00], round.block: 0.00 [+- 0.00], round.gc: 0.00 [+- 0.00], GC.calls: 55, GC.time: 0.16, time.total: 4.30, time.warmup: 0.00, time.bench: 4.30
    HyperLogLogSerdeBenchmarkTest.benchmarkToByteBuffer[4]: [measured 100000 out of 100100 rounds, threads: 1 (sequential)]
     round: 0.00 [+- 0.00], round.block: 0.00 [+- 0.00], round.gc: 0.00 [+- 0.00], GC.calls: 8, GC.time: 0.03, time.total: 1.10, time.warmup: 0.00, time.bench: 1.09
    HyperLogLogSerdeBenchmarkTest.benchmarkToByteBuffer[5]: [measured 100000 out of 100100 rounds, threads: 1 (sequential)]
     round: 0.00 [+- 0.00], round.block: 0.00 [+- 0.00], round.gc: 0.00 [+- 0.00], GC.calls: 8, GC.time: 0.03, time.total: 0.72, time.warmup: 0.00, time.bench: 0.72
    HyperLogLogSerdeBenchmarkTest.benchmarkToByteBuffer[6]: [measured 100000 out of 100100 rounds, threads: 1 (sequential)]
     round: 0.00 [+- 0.00], round.block: 0.00 [+- 0.00], round.gc: 0.00 [+- 0.00], GC.calls: 1, GC.time: 0.00, time.total: 0.60, time.warmup: 0.00, time.bench: 0.60
    HyperLogLogSerdeBenchmarkTest.benchmarkToByteBuffer[7]: [measured 100000 out of 100100 rounds, threads: 1 (sequential)]
     round: 0.00 [+- 0.00], round.block: 0.00 [+- 0.00], round.gc: 0.00 [+- 0.00], GC.calls: 2, GC.time: 0.01, time.total: 0.26, time.warmup: 0.00, time.bench: 0.25

    Updates to HyperLogLogCollector toByteBuffer() based on code review

    Removed changes from DirectDruidClient from this branch and put it in another branch.

    Changed HyperLogLogCollector to have protected getters and setters

    Remove unused ByteOrder from HyperLogLogCollector

    Copyright header on HyperLogLogSerdeBenchmarkTest

    Now with less ass!

    Reformat in TopNNumericResultsBuilder. No code change

    Removed unused import in HyperLogLogCollector

    Replace AppendableByteArrayInputStream in DirectDruidClient
    * Replace with SequenceInputStream fueled by an enumeration of ChannelBufferInputStream which directly wrap the response context ChannelBuffer

    Modify TopNQueryQueryToolChest to use Arrays instead of Lists

    Modify TopNQueryQueryToolChest to use Arrays instead of Lists

    Revert accidental changes to DirectDruidClient

    They should be in another merge request:
    https://github.com/metamx/druid/pull/893

    Fixes from code review
    * Extracting names from AggregatorFactory classes now done with TopNQueryQueryToolChest.extractFactoryName
    * Renamed variable in TopNNumericResultBuilder