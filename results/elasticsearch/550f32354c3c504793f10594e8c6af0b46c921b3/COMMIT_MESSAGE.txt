commit 550f32354c3c504793f10594e8c6af0b46c921b3
Author: Ali Beyad <ali@elastic.co>
Date:   Fri Feb 24 14:55:42 2017 -0500

    [TEST] Removes timeout based wait_for_active_shards REST test (#23360)

    This commit removes an necessary test that ensures if
    wait_for_active_shards cannot be fulfilled on index creation, that the
    response returns shardsAcknowledged=false.  However, this is already
    tested in WaitForActiveShardsIT and it would improve the speed of the
    test runs to get rid of any unnecessary tests, especially those that
    depend on timeouts.