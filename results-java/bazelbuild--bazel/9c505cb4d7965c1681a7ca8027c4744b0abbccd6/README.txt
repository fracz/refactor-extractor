commit 9c505cb4d7965c1681a7ca8027c4744b0abbccd6
Author: Ulf Adams <ulfjack@google.com>
Date:   Mon Sep 28 13:38:14 2015 +0000

    BuildView; move the methods around.

    Production API at the top, then ide_build_info, and testing at the bottom.
    This is separate from the refactoring to make both easier to review.

    --
    MOS_MIGRATED_REVID=104095498