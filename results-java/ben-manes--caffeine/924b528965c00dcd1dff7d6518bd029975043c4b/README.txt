commit 924b528965c00dcd1dff7d6518bd029975043c4b
Author: Ben Manes <ben.manes@gmail.com>
Date:   Sat Dec 12 12:30:52 2015 -0800

    Avoid dead code elimination in benchmarks (fixes #39)

    Most benchmarks aren't effected due to making stateful changes, such as
    cache reads updating the policy. It is very observable in the frequency
    sketch (from 520M -> 60M) where the benchmarks are used for tuning. That
    shows that the optimizations of Unsafe where not helpful, but actually
    aiding the JVM to perform the elimination. So this fix helps simplify
    the code, improves accuracy, and is stylistically better.