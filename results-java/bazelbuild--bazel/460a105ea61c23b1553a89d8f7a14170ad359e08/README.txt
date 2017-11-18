commit 460a105ea61c23b1553a89d8f7a14170ad359e08
Author: olaola <olaola@google.com>
Date:   Fri Jun 9 04:33:25 2017 +0200

    Switching Bazel to use the new remote execution API: https://docs.google.com/document/d/1AaGk7fOPByEvpAbqeXIyE8HX_A3_axxNnvroblTZ_6s/edit

    Also refactored away the various *Interface* files, no need since unit testing can be done with mocking the appropriate gRPC Impl classes directly (see tests). This also fixes the RemoteSpawnRunner, which should use different objects for remote caching and remote execution, the same way RemoteSpawnStrategy does.

    RELNOTES: n/a
    PiperOrigin-RevId: 158473700