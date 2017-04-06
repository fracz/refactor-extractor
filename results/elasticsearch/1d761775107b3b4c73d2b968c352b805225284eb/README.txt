commit 1d761775107b3b4c73d2b968c352b805225284eb
Author: Colin Goodheart-Smithe <colings86@users.noreply.github.com>
Date:   Tue May 17 14:16:20 2016 +0100

    Adds aggregation profiling (not including reduce phase)

    Add Aggregation profiling initially only be for the shard phases (i.e. the reduce phase will not be profiled in this change)

    This change refactors the query profiling class to extract abstract classes where it is useful for other profiler types to share code.