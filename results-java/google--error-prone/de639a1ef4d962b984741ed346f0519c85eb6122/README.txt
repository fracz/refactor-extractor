commit de639a1ef4d962b984741ed346f0519c85eb6122
Author: glorioso <glorioso@google.com>
Date:   Fri Oct 7 17:54:34 2016 -0700

    Add a new Error Prone Option: -XepPatch

    which will apply all of the suggested fixes to the compiled sources.

    RELNOTES: Add an experimental option: -XepPatch, that will apply all of
    the suggested fixes from Error Prone to the source.

    Usages:
    -XepPatch:/absolute/path/to/source/root - This will generate a unixpatch at /absolute/path/to/source/root/error-prone.patch
    -XepPatch:IN_PLACE - This will refactor in place

    MOE_MIGRATED_REVID=137573513