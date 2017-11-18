commit b6e436e81374e243eca68c2264af3bf0e7bf6b6b
Author: Dianne Hackborn <hackbod@google.com>
Date:   Wed Nov 16 13:21:46 2011 -0800

    Maybe fix issue #5627399: java.lang.RuntimeException - While sign in...

    ...through setup wizard after wipe data

    Deal with finish() being called when there are no running activities
    on the stack.

    Also some improved debugging output.

    Change-Id: Ia1d3f3f7e7b79c06ca95c738081322fc80282e0d