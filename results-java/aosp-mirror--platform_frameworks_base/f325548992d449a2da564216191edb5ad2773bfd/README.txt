commit f325548992d449a2da564216191edb5ad2773bfd
Author: Jorim Jaggi <jjaggi@google.com>
Date:   Fri Jul 24 12:32:42 2015 -0700

    Fix ambient display

    Due to a refactoring for touch, wake and unlock for fingerprint
    devices PhoneWindowManager now waits for Keyguard to draw its first
    frame in all cases, including when screen turns on for pulsing.
    However, since in this case the device is not awake, we need to check
    for screen on instead of awakeness when proceeding with the wake-up
    sequence. Without this change, screen on was blocked forever in the
    pulsing case.

    Bug: 22605235
    Change-Id: Ib6089fd14b673e62347f2c9108d9a6783caa26b7