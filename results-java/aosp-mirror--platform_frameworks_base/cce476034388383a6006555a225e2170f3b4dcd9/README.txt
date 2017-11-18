commit cce476034388383a6006555a225e2170f3b4dcd9
Author: Christopher Tate <ctate@google.com>
Date:   Mon Aug 4 14:49:26 2014 -0700

    Sanity-check paths of files to be restored

    The duplicated implementations are an artifact of an ongoing
    refactor of the full-data restore code.  The adb-specific path
    will be switched to use the FullRestoreEngine [as has already
    been done for the 'adb backup' path using the parallel full
    backup engine], at which point the extra implementation here
    will be removed, but for now we need to make sure that all
    bases are covered.

    Bug 16298491

    Change-Id: I9cdb8a1c537939a620208df3cf0e921061b981ad