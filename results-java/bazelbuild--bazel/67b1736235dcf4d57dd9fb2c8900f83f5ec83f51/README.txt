commit 67b1736235dcf4d57dd9fb2c8900f83f5ec83f51
Author: kmb <kmb@google.com>
Date:   Mon Aug 7 21:13:29 2017 +0200

    improve efficiency of no-op desugarings
    - skip lambda desugaring when it won't do anything
    - skip ASM class writing when no desugarings apply to an input class
    also minor improvements to prefix remapping
    RELNOTES: none

    PiperOrigin-RevId: 164492293