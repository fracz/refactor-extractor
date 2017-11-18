commit 1c2ac49f2ac32ab0ee589bfc165046c14aa782c7
Author: Brian Clozel <bclozel@pivotal.io>
Date:   Thu Feb 18 16:06:38 2016 +0100

    Add weak ETag support in ShallowEtagHeaderFilter

    This commit adds weak ETag support in ShallowEtagHeaderFilter.
    This improves the behavior of the filter in tow ways:

    * weak ETags in request headers such as `W/"0badc0ffee"` will be
    compared with a "weak comparison" (matching both weak and strong ETags
    of the same value)
    * when enabled with the "writeWeakETag" init param, the filter will
    write weak Etags in its HTTP responses

    Issue: SPR-13778