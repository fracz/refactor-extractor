commit 550267f72b6217f797e7f93bb312ecbe6541ff49
Author: Alan Viverette <alanv@google.com>
Date:   Fri Feb 21 16:48:17 2014 -0800

    Fix refactoring of invalidate methods

    Was incorrectly clearing the DRAWN flag and updating mLastIsOpaque from
    partial invalidations, though why this should be different is somewhat
    of a mystery.

    BUG: 13138721
    Change-Id: Ic8d11a64406bc78e94adec7355c1f50d87567887