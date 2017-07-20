commit e5fd95f234a068ca67387a3a30ce5e32c417ef05
Author: Thomas Steur <thomas.steur@googlemail.com>
Date:   Thu Jul 3 04:54:09 2014 +0200

    fix showHideColumns test, the cache key did not depend on those parameters which are now considered. also more performance improvements in hideShowMetrics by doing certain actions not again for each report