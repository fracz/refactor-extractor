commit be6a71a0b3f369843a26c91dd5123d0499f00e7e
Author: Calin Juravle <calin@google.com>
Date:   Thu Dec 22 18:50:05 2016 +0200

    Some refactoring in BackgroundDexOptService.

    Extract postOta/idle optimizations in their own method.
    In preparation for adding the logic to handle secondary dex files.

    Test: device boots, pacakges get compiled
    Bug: 32871170
    Change-Id: Iee12ab90215b60ea00ed54ca2d8c4f526036058c