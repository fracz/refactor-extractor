commit 0805d36388ef9724e3c5894616255ed0849ba4c8
Author: James Sun <jamessun@fb.com>
Date:   Mon Feb 27 19:48:31 2017 -0800

    Add benchmark for TransformKey and TransformValue

    Two new benchmarks added for evaluating the possible outcome for
    bytecode generation for map transform key/value. The results shows
    a 10%-25% improvement.

    BenchmarkTransformKey is to transform a long type key x to x + 1.
    BenchmarkTransformValue is to transform a long type value y to a
    boolean type if y > 0.

    Benchmark     (name)  (type)  Mode  Cnt    Score   Error  Units
          transform_keys  DOUBLE  avgt   20  143.077 ± 2.378  ns/op
    exact_transform_keys  DOUBLE  avgt   20  136.607 ± 7.131  ns/op
          transform_keys  BIGINT  avgt   20  107.877 ± 5.898  ns/op
    exact_transform_keys  BIGINT  avgt   20   92.876 ± 4.231  ns/op

    Benchmark       (name)   (type)  Mode  Cnt   Score   Error  Units
          transform_values   BIGINT  avgt   20  67.534 ± 4.069  ns/op
    exact_transform_values   BIGINT  avgt   20  50.477 ± 2.556  ns/op
          transform_values   DOUBLE  avgt   20  62.217 ± 1.273  ns/op
    exact_transform_values   DOUBLE  avgt   20  50.615 ± 3.348  ns/op
          transform_values  VARCHAR  avgt   20  89.682 ± 3.663  ns/op
    exact_transform_values  VARCHAR  avgt   20  76.351 ± 3.868  ns/op