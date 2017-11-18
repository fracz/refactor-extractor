commit eb62df758064852a95a8095b4d3b2ff61a225cd4
Author: jasexton <jasexton@google.com>
Date:   Thu Aug 18 10:20:50 2016 -0700

    Optimize directed graphs to consume less memory, at the cost of additional code complexity. Some very rough benchmarks also seem to indicate this has slightly faster runtime. I suspect this is due to improved cache performance/locality when using a single Map.

    Memory: Directed mutable graphs consume less memory across the board, with graphs of small degree benefiting the most (memory consumption around 75% of previous). Directed immutable graph memory consumption is more of a wash - going down slightly for graphs of small degree and up for graphs of large degree.

    Runtime: Adding a few million edges seems to be about 10% faster.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=130653738