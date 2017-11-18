commit 397567d2c29fbe2fce13ac8e2da2029358ed3cd9
Author: Max Medvedev <maxim.medvedev@jetbrains.com>
Date:   Fri Apr 11 09:54:26 2014 +0400

    tuple and map types improved.
    Used VolatileNotNullLazyValue instead of AtomicNotNullLazyValue to avoid deadlock
    Got rid of GrTupleTypeWithLazyValue. All the functionality is moved to GrTupleType