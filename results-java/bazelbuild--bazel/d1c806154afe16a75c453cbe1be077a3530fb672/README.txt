commit d1c806154afe16a75c453cbe1be077a3530fb672
Author: Googler <noreply@google.com>
Date:   Mon Mar 23 13:08:09 2015 +0000

    Skylark: the native module is refactored and documented.

     - SkylarkNativeModule is added to handle to native module.
     - Glob function is migrated to be a SkylarkFunction. Note that other functions in the native module are more difficult to migrate since they are not static.

    --
    MOS_MIGRATED_REVID=89292579