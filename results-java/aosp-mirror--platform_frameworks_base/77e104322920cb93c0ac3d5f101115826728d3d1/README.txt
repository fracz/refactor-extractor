commit 77e104322920cb93c0ac3d5f101115826728d3d1
Author: Jorim Jaggi <jjaggi@google.com>
Date:   Wed Oct 26 17:43:56 2016 -0700

    The big keyguard transition refactor (3/n)

    Notify activity manager when dreaming showing state changed so
    KeyguardController can update the occluded state when the device
    is dreaming.

    Test: Set dreaming while charging, wait until screen times out,
    make sure that dream is occluding Keyguard.
    Bug: 32057734
    Change-Id: Ied6f485d9b4a1526cb4cd5f0701f86b1ea05830a