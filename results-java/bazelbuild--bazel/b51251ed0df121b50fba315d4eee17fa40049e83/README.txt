commit b51251ed0df121b50fba315d4eee17fa40049e83
Author: Michael Staib <mstaib@google.com>
Date:   Tue Sep 29 23:31:51 2015 +0000

    Extract configuration fragment access logic into ConfigurationFragmentPolicy.

    This is the first step toward giving aspects the ability to define their own
    required configuration fragments, extracting the required configuration
    metadata into a common place.

    This should be a no-op refactoring.

    --
    MOS_MIGRATED_REVID=104249500