commit 6d28a93e210c0a201e5dea6d45bdbfd3678bc0c2
Author: Carl Mastrangelo <notcarl@google.com>
Date:   Fri Aug 26 17:32:31 2016 -0700

    core: simplify timeout header processing

    Changes slightly improve performance

    Benchmark                    (serialized)    Mode     Cnt    Score    Error  Units
    GrpcUtilBenchmark.encodeNew         1000n  sample  336623   51.718 ±  1.417  ns/op
    GrpcUtilBenchmark.encodeNew         1000u  sample  236574   77.555 ± 20.875  ns/op
    GrpcUtilBenchmark.encodeNew         1000m  sample  224392   71.155 ±  1.600  ns/op
    GrpcUtilBenchmark.encodeNew         1000S  sample  229616   67.269 ±  2.037  ns/op
    GrpcUtilBenchmark.encodeNew         1000M  sample  215301   70.282 ±  1.933  ns/op
    GrpcUtilBenchmark.encodeNew         1000H  sample  225063   73.679 ± 20.430  ns/op
    GrpcUtilBenchmark.encodeOld         1000n  sample  311832   85.519 ±  1.729  ns/op
    GrpcUtilBenchmark.encodeOld         1000u  sample  291613   92.320 ±  1.732  ns/op
    GrpcUtilBenchmark.encodeOld         1000m  sample  271871   93.447 ±  1.872  ns/op
    GrpcUtilBenchmark.encodeOld         1000S  sample  234932  117.956 ± 16.810  ns/op
    GrpcUtilBenchmark.encodeOld         1000M  sample  224636  124.310 ± 20.249  ns/op
    GrpcUtilBenchmark.encodeOld         1000H  sample  226764  130.803 ± 19.211  ns/op
    GrpcUtilBenchmark.parseNew          1000n  sample  320709   60.480 ±  1.303  ns/op
    GrpcUtilBenchmark.parseNew          1000u  sample  316349   64.447 ± 13.673  ns/op
    GrpcUtilBenchmark.parseNew          1000m  sample  318209   61.705 ±  2.580  ns/op
    GrpcUtilBenchmark.parseNew          1000S  sample  319629   59.342 ±  1.758  ns/op
    GrpcUtilBenchmark.parseNew          1000M  sample  305715   59.362 ±  1.489  ns/op
    GrpcUtilBenchmark.parseNew          1000H  sample  314919   60.224 ±  1.563  ns/op
    GrpcUtilBenchmark.parseOld          1000n  sample  279243   64.040 ±  1.510  ns/op
    GrpcUtilBenchmark.parseOld          1000u  sample  278008   71.313 ± 13.620  ns/op
    GrpcUtilBenchmark.parseOld          1000m  sample  272633   67.872 ±  2.967  ns/op
    GrpcUtilBenchmark.parseOld          1000S  sample  280955   63.966 ±  2.490  ns/op
    GrpcUtilBenchmark.parseOld          1000M  sample  257645   71.329 ±  2.117  ns/op
    GrpcUtilBenchmark.parseOld          1000H  sample  282510   68.425 ± 17.650  ns/op