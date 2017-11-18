commit 9211b13c3268035b0da0c51ed2d6d5a578d45ff3
Author: rikard dahlman <rikard.dahlman@sonymobile.com>
Date:   Tue Aug 28 16:12:38 2012 +0200

    Watchdog: Improvement of debuggability

    If the watchdog detects a problem the system server process
    is killed, that is followed by a crash. Because the crash is
    done after the system server process is killed, the crash
    don't contain info about the system server.
    This improvement will make sure that the system is crashed
    before the system server process is killed.
    Behavior is only changed for eng and userdebug builds.

    Change-Id: I9f1c8fd8b03d0114032ed44fb582705ad0b49733