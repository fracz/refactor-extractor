commit cdf980dbcf01ab951927bbe59b54ba6db6e1a19f
Author: Chris Vest <mr.chrisvest@gmail.com>
Date:   Fri Sep 27 16:28:23 2013 +0200

    ExcpetionHandlingIterable no longer uses Unsafe.

    It was an ugly hack before, and it's still an ugly hack, but now
    it is an ugly hack that does not use the Unsafe. So a small
    improvement.

    Really though, we should do something about the fact that we expect
    checked expceptions through the Iterable methods.