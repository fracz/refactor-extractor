commit a88d843828f958783028e6604e1e6ffd116465de
Author: emcmanus <emcmanus@google.com>
Date:   Thu Apr 21 14:33:13 2016 -0700

    Restructure the code in AutoValueProcessor for handling extensions. This involves moving a fair amount of code around, but the resultant organization for method identification should be clearer. The principal purpose of the restructuring is to get rid of warnings about abstract methods when those methods are going to be implemented by an extension. A second purpose is to fix a bug where extensions would not work right if there was a toBuilder() method.

    Also improve the test coverage in ExtensionTest, which was fairly minimal.

    Some of the code in this change is based on https://github.com/google/auto/pull/299 by @rharter.
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=120485500