commit 9c98f70db1b5073341184cb061fdc5f7f4b884fc
Author: Adrian Cole <adriancole@users.noreply.github.com>
Date:   Mon Dec 19 17:02:13 2016 +0800

    Adds Span.Builder.clear() (#1442)

    Unlike Endpoint, Annotation, etc Span cannot be composed without a
    builder. This makes it possible to reuse the builder which improves
    straight-line performance quite a bit.

    ```
    Benchmark                                  Mode  Cnt  Score   Error   Units
    SpanBenchmarks.buildClientOnlySpan        thrpt   15  6.088 ± 0.161  ops/us
    SpanBenchmarks.buildClientOnlySpan_clear  thrpt   15  8.080 ± 0.227  ops/us
    ```