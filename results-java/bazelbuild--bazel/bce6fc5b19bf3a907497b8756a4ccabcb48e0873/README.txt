commit bce6fc5b19bf3a907497b8756a4ccabcb48e0873
Author: Janak Ramakrishnan <janakr@google.com>
Date:   Thu Aug 18 16:46:09 2016 +0000

    Refactor GlobFunction to avoid random-access lookup of the returned map of a SkyFunction.Environment#getValues call. In future, we may not want to pay the cost of constructing a true map in SkyFunction.Environment implementations. This is a preliminary refactor to allow that.

    --
    MOS_MIGRATED_REVID=130649663