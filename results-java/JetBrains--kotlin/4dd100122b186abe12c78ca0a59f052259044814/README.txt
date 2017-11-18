commit 4dd100122b186abe12c78ca0a59f052259044814
Author: Dmitry Petrov <dmitry.petrov@jetbrains.com>
Date:   Wed Jan 11 17:11:41 2017 +0300

    Explicitly remove NOPs inserted for bytecode analysis in post-conditional loops.
    Remove redundant NOPs during bytecode optimization.

    NOP instruction is required iff one of the following is true:
    (a) it is a first bytecode instruction in a try-catch block (JVM BE assumption);
    (b) it is a sole bytecode instruction in a source code line (breakpoints on that line will not work).
    All other NOP instructions can be removed.

    Note that it doesn't really affect the performance for mature JVM implementations.
    However, the perceived quality of the generated code is somewhat improved :).

    Related: KT-15609