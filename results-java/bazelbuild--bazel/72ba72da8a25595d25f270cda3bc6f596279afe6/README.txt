commit 72ba72da8a25595d25f270cda3bc6f596279afe6
Author: cparsons <cparsons@google.com>
Date:   Wed Apr 5 14:12:20 2017 +0000

    Slightly improve efficiency of apple_binary dylib-proto-dependency subtraction

    (Pull NestedSet creation outside of a loop)

    RELNOTES: None.

    PiperOrigin-RevId: 152253535