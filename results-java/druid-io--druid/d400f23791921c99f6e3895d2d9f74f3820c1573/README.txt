commit d400f23791921c99f6e3895d2d9f74f3820c1573
Author: Roman Leventov <leventov.ru@gmail.com>
Date:   Tue May 16 18:19:55 2017 -0500

    Monomorphic processing of TopN queries with simple double aggregators over historical segments (part of #3798) (#4079)

    * Monomorphic processing of topN queries with simple double aggregators and historical segments

    * Add CalledFromHotLoop annocations to specialized methods in SimpleDoubleBufferAggregator

    * Fix a bug in Historical1SimpleDoubleAggPooledTopNScannerPrototype

    * Fix a bug in SpecializationService

    * In SpecializationService, emit maxSpecializations warning only once

    * Make GenericIndexed.theBuffer final

    * Address comments

    * Newline

    * Reapply 439c906 (Make GenericIndexed.theBuffer final)

    * Remove extra PooledTopNAlgorithm.capabilities field

    * Improve CachingIndexed.inspectRuntimeShape()

    * Fix CompressedVSizeIntsIndexedSupplier.inspectRuntimeShape()

    * Don't override inspectRuntimeShape() in subclasses of CompressedVSizeIndexedInts

    * Annotate methods in specializations of DimensionSelector and FloatColumnSelector with @CalledFromHotLoop

    * Make ValueMatcher to implement HotLoopCallee

    * Doc fix

    * Fix inspectRuntimeShape() impl in ExpressionSelectors

    * INFO logging of specialization events

    * Remove modificator

    * Fix OrFilter

    * Fix AndFilter

    * Refactor PooledTopNAlgorithm.scanAndAggregate()

    * Small refactoring

    * Add 'nothing to inspect' messages in empty HotLoopCallee.inspectRuntimeShape() implementations

    * Don't care about runtime shape in tests

    * Fix accessor bugs in Historical1SimpleDoubleAggPooledTopNScannerPrototype and HistoricalSingleValueDimSelector1SimpleDoubleAggPooledTopNScannerPrototype, cover them with tests

    * Doc wording

    * Address comments

    * Remove MagicAccessorBridge and ensure Offset subclasses are public

    * Attach error message to element