commit 18cf923072a02d9222ea52eb47fa011d7b957090
Author: Jesse Vincent <jesse@fsck.com>
Date:   Sun Jun 6 21:32:57 2010 +0000

    Only strip : lines containing word characters. This should improve the
    preview when someone sends mail starting with a time. (12:01)