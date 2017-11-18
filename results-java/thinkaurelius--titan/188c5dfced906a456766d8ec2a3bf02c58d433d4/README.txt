commit 188c5dfced906a456766d8ec2a3bf02c58d433d4
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Thu Sep 19 04:57:04 2013 -0400

    Added JUnitBenchmarks method runtime normalization

    I added a new implementation of JUB's IResultsConsumer interface to
    JUnitBenchmarkProvider.  This consumer gets execution data from JUB
    and writes the method classname, method name, and 1000 / runtime_in_ms
    to a file, in that order, separated by spaces.  JUnitBenchmarkProvider
    automatically enables this consumer when the environment variable
    JUB_WRITE_SCALARS is set and contains a valid file path.  It appends
    to the file rather than overwriting it.

    JUnitBenchmarkProvider can also parse this file via a static helper
    method, returning a map of classname.methodname to millisecond
    runtime.

    This commit also contains some minor refactoring in GroovySerialTest
    and GroovyTestSupport.