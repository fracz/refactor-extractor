commit c1b50324a2286b24b691b8a7190743cbc341727e
Author: Adrian Roos <roosa@google.com>
Date:   Mon Feb 27 21:07:58 2017 +0100

    AOD: Add wakelock for charging text while dozing

    Also refactors the WakeLocks in SystemUI.

    Bug: 30876804
    Bug: 35850304
    Test: runtest systemui
    Change-Id: Ie17eedfd266deb3aa46dabd701bc784330b2e030