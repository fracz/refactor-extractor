commit e0e2b4174faddb26f15bd072908aeb21dfe08266
Author: Todd Christensen <tchristensen@bykd.com>
Date:   Tue Mar 24 19:23:14 2015 -0700

    Foundation: use a lookup for app->getProvider().

    This greatly improves the performance of getProvider(), and therefore
    register().

    In one application with only a few more providers than the default,
    its 163 tests complete about 8% faster with this change applied.