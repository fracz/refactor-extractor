commit eaa10330ec3b409681ef98eadce0a722b04022a5
Author: Christopher Berner <cberner@fb.com>
Date:   Wed Jul 16 18:11:13 2014 -0700

    Improve aggregation framework

    * Introduce a compiler for accumulators
    * Introduce compiler for aggregation functions
    * Add annotations, so that aggregation functions can be defined as a combination of static methods
    * Benchmarks show ~10% performance improvement on TPCH query 1, and ~30% speed up for SUM()