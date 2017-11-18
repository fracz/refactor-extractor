commit 15d32ee93f8dd02cee815c3ecf2a1dd76a13e7e0
Author: Karol Sobczak <Karol.Sobczak@Teradata.com>
Date:   Thu Oct 20 15:39:53 2016 +0200

    Make HashGenerationOptimizer insensitive to hash symbols order

    This improvement reduces number of hash computations in a plan
    when hash symbols for partitions or joins are the same but
    with different order (e.g: Tpch Q9 query).