commit 53459a7020dbcd036e2d3418e35ebb96fadc29e3
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Sep 17 17:14:57 2013 -0700

    Maybe fix issue #10797796: IllegalStateException in ProcessState...

    ...caused runtime restart

    There were some situations where the package list could be set
    with process stats when it shouldn't.  Not sure if this is causing
    the problem, since there is no repro.

    Also some improvements to debug output -- new commands to clear
    all stats, print full details of stats, and print a one-day
    summary (which should match what the UI shows).

    Change-Id: I9581db4059d7bb094f79f2fe06c1ccff3e1a4e74