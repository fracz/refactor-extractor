commit a4eab42fe437bff3f8ee9dde264579067ea5cdbd
Author: Alan Viverette <alanv@google.com>
Date:   Mon Jun 9 19:31:20 2014 -0700

    Change ripple tint to color, remove tintMode

    Also fixes double ripple on list preferences, missing ripple on up
    button, and adds the Toolbar style to public. Further improves
    ripple performance.

    BUG: 15523923
    BUG: 15473856
    Change-Id: I5e8bf417368b60fcc33c80852e12f27b8c580774