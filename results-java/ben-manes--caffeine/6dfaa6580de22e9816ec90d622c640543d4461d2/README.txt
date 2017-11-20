commit 6dfaa6580de22e9816ec90d622c640543d4461d2
Author: Ben Manes <ben.manes@gmail.com>
Date:   Sat Aug 29 18:16:21 2015 -0700

    Progress on the simulator's LIRS policy

    This appears to be correct based on the papers and comparing the steps
    against the reference implementations. However there are different hit
    rates between implementations, so this must be investigated. It appears
    that the others do not prune the stack after an eviction, so the demoted
    LIRS entry may be a HIRS. This seems to go against the description in
    the papers, but improves the hit rate.

    The next step is to see if we can get the same hit rate and internal
    state as the author's C version. When the Java version matches, then
    we can see if the reference versions are better and adapt their changes.

    Renamed the trace files for consistency. Now all are named using the
    format `{name}.trace.gz`.