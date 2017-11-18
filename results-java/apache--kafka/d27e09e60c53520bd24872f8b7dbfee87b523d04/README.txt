commit d27e09e60c53520bd24872f8b7dbfee87b523d04
Author: Ismael Juma <ismael@juma.me.uk>
Date:   Mon Mar 27 09:28:46 2017 -0700

    MINOR: Use method handles instead of reflection for creating Snappy and LZ4 streams

    1. Use Initialization-on-demand holder idiom that relies on JVM lazy-loading instead of explicit initialization check.
    2. Method handles were designed to be faster than Core Reflection, particularly if the method handle can be stored in a static final field (the JVM can then optimise the call as if it was a regular method call). Since the code is of similar complexity (and simpler if we consider the whole PR), I am treating this as a clean-up instead of a performance improvement (which would require doing benchmarks).
    3. Remove unused `ByteBufferReceive`.
    4. I removed the snappy library from the classpath and verified that `CompressionTypeTest` (which uses LZ4) still passes. This shows that the right level of laziness is achieved even if we use one of the lazily loaded compression algorithms.

    Author: Ismael Juma <ismael@juma.me.uk>

    Reviewers: Jason Gustafson <jason@confluent.io>

    Closes #2740 from ijuma/use-method-handles-for-compressed-stream-supplier