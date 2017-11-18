commit e5d95fb510ae056c8650b5bb450ec5dc7c7eeb3b
Author: brandjon <brandjon@google.com>
Date:   Thu Jul 13 17:23:09 2017 +0200

    Fix crash when unioning depsets with different orders

    Also refactor FAIL_FAST_HANDLER to throw something more specific than IllegalArgumentException. This bug was masked because the test assertion that would've caught it considered IllegalArgumentException to be an expected error, the same as EvalException.

    RELNOTES: None
    PiperOrigin-RevId: 161809957