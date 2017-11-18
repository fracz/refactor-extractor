commit af93a8d1894374b69adfef9e086fb925a7bc27bd
Author: Roman Leventov <leventov@users.noreply.github.com>
Date:   Thu Jan 19 22:07:43 2017 -0600

    Sequences refactorings and removed unused code (part of #3798) (#3693)

    * Removing unused code from io.druid.java.util.common.guava package; fix #3563 (more consistent and paranoiac resource handing in Sequences subsystem); Add Sequences.wrap() for DRY in MetricsEmittingQueryRunner, CPUTimeMetricQueryRunner and SpecificSegmentQueryRunner; Catch MissingSegmentsException in SpecificSegmentQueryRunner's yielder.next() method (follow up on #3617)

    * Make Sequences.withEffect() execute the effect if the wrapped sequence throws exception from close()

    * Fix strange code in MetricsEmittingQueryRunner

    * Add comment on why YieldingSequenceBase is used in Sequences.withEffect()

    * Use Closer in OrderedMergeSequence and MergeSequence to close multiple yielders