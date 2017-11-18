commit 9ff53a7100b1a40f5d2df3eb19a2f3f2fff39a46
Author: Christopher Tate <ctate@google.com>
Date:   Tue Jun 3 17:20:07 2014 -0700

    Implement full data backup through transport

    Currently no timed/scheduled full-data backup operations are
    performed by the OS, but the plumbing is now in place and can
    be tested using 'adb shell bmgr fullbackup pkg [pkg2 pkg3 ...]'.

    The LocalTransport test transport implementation has been augmented
    to support the new full-data backup API as well.

    In addition, 'adb backup' now takes the -compress/-nocompress
    command line options to control whether the resulting archive is
    compressed before leaving the device.  Previously the archive was
    always compressed.  (The default is still to compress, as it will
    usually reduce the archive size considerably.)

    Internally, the core implementation of gathering the full backup
    data stream from the target application has been refactored into
    an "engine" component that is shared by both 'adb backup' and the
    transport-oriented full backup task.  The archive file header
    generation, encryption, and compression logic are now factored out
    of the engine itself instead of being hardwired into the data
    handling.

    Bug 15329632

    Change-Id: I4a044faa4070d684ef457bd3e11771198cdf557c