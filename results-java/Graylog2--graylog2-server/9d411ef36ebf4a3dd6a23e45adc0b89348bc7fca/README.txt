commit 9d411ef36ebf4a3dd6a23e45adc0b89348bc7fca
Author: Jochen Schalanda <jochen@schalanda.name>
Date:   Fri Jun 3 14:35:11 2016 +0200

    Make MemoryAppender thread-safe (#2307)

    * Make MemoryAppender thread-safe
    * improve thread safety test to make the original implementation fail much quicker and more reliably

    Fixes #2302