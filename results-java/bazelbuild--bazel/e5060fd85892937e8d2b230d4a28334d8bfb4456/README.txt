commit e5060fd85892937e8d2b230d4a28334d8bfb4456
Author: Janak Ramakrishnan <janakr@google.com>
Date:   Fri Aug 19 17:26:24 2016 +0000

    Refactor ActionExecutionFunction to avoid unnecessary unwrapping of SkyKeys to Artifacts. Also avoid overhead of creating a set when we only need an iterable, and avoid an unnecessary iteration over the iterable as a contains check. In future, we may not want to pay the cost of constructing a true map in SkyFunction.Environment implementations. Part of this is a preliminary refactor to allow that.

    --
    MOS_MIGRATED_REVID=130765995