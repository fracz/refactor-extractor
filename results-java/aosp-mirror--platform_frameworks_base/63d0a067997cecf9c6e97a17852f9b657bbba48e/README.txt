commit 63d0a067997cecf9c6e97a17852f9b657bbba48e
Author: Jeff Sharkey <jsharkey@android.com>
Date:   Fri Mar 1 16:12:55 2013 -0800

    Improve performance of storage measurement.

    When calculating directory sizes of data living on emulated external
    storage, translate the path to use the internal backing data, which
    avoids going through the emulation layer.  It carefully retreats to
    the original path when it runs into trouble.

    Testing with a hierarchy of 10 directories deep and 2 directories
    and 10 files wide at each level, this change improves performance
    from 5900ms before to 250ms after; over 20 times faster (!).

    Bug: 8172425
    Change-Id: Ia7365416f091e102bf7345a49f7d7209a22580a9