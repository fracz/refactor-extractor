commit a7f3a34313074a96b1cc2eb4bea0304d8375af75
Author: Jim Miller <jaggies@google.com>
Date:   Mon Oct 17 17:37:28 2011 -0700

    Fix 5473038: workaround for fd leak in lockscreen

    This works around a file descriptor leak in Skia. It also improves
    view creation time by avoiding re-loading the font every time a
    DigitalClock is created.

    Change-Id: I5c46445da36b1e6ba06c8ca340e436835d281180