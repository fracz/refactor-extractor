commit 95176bb18e5efeb78978f4950a18fadaf9e6d57e
Author: Calin Juravle <calin@google.com>
Date:   Thu Dec 22 18:50:05 2016 +0200

    Some refactoring in BackgroundDexOptService.

    Extract postOta/idle optimizations in their own method.
    In preparation for adding the logic to handle secondary dex files.

    Test: device boots, pacakges get compiled
    Bug: 32871170

    (cherry picked from commit be6a71a0b3f369843a26c91dd5123d0499f00e7e)

    Change-Id: Ie6cdd8461e7214f5de68bc9172f4171ebf72aa39