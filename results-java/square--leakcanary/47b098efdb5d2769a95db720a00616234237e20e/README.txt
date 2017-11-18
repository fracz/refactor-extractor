commit 47b098efdb5d2769a95db720a00616234237e20e
Author: Pierre-Yves Ricau <py@squareup.com>
Date:   Sun Jul 19 09:39:35 2015 -0700

    Swiching to HAHA 2.0.2 / Perflib

    * Switched to HAHA 2.0.2 which is based on perflib instead of MAT
    * AnalysisResult.failure is now a `Throwable` instead of an `Exception`. Main goal is to catch and correctly report OOMs while parsing.
    * Added ARRAY_ENTRY to LeakTraceElement.Type for references through array entries.
    * Huge speed improvements when performing the shortest path with excluded references: it's now tailored to our needs instead of doing it on a loop.
    * Renamed `ExcludedRefs` fields.
    * Each `ExcludedRef` entry can now be ignored entirely or "kept only if no other path".
    * Added support for ignoring all fields (static and non static) for a given class.
    * Improved the heap dump analyzer test and added a heap dump for M Preview 2.
    * Bumped version to 1.4-SNAPSHOT because this breaks the APIs.