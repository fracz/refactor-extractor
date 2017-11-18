commit e0b0bef75b66f0a87039c8f58c17b1596a2baebe
Author: Jeff Sharkey <jsharkey@android.com>
Date:   Sat Jul 12 15:37:47 2014 -0700

    Surface detailed error messages from PMS.

    We now both log detailed error messages and relay them back to any
    observer.  Start refactoring PMS to throw when errors are encountered
    internally to make it easier to reason about flow control; already
    uncovered a few instances of errors being silently ignored!

    Change-Id: Ia335c5e31bd10243d52fd735c513ca828e83dca0