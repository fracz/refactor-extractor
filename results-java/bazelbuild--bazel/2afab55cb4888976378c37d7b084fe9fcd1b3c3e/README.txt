commit 2afab55cb4888976378c37d7b084fe9fcd1b3c3e
Author: Philipp Wollermann <philwo@google.com>
Date:   Tue May 9 08:29:17 2017 -0400

    sandbox: Use process-wrapper in addition to sandbox-exec on macOS.

    This gives us much improved process management, because Bazel can now
    reliably kill child processes of spawns via their process group and wait
    for them to exit.

    Change-Id: Ib3cb20725b3c569aa5b317a69d7682f5774707b0
    PiperOrigin-RevId: 155493511