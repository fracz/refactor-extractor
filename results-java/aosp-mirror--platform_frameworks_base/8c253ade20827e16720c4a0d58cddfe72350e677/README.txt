commit 8c253ade20827e16720c4a0d58cddfe72350e677
Author: Lorenzo Colitti <lorenzo@google.com>
Date:   Wed Jul 19 00:23:44 2017 +0900

    Minor fixes for netd restarts and StrictController.

    1. Ensure we don't change strict mode network policy for a given
       UID from a non-accept policy to another non-accept policy,
       as netd does not support this.
    2. Move the "strict enable" and "bandwidth enable" commands
       inside the lock. This improves correctness, and it is safe to
       do now that those commands now only take a few milliseconds,
       instead of several hundred milliseconds.
    3. Fix an NPE in connectNativeNetdService which causes the system
       to crash when netd crashes.

    Bug: 28362720
    Test: bullhead builds, boots
    Test: "adb shell killall netd" no longer crashes the system
    Change-Id: Icdaa9d1e2288accf35de21df56bc6bd2b0628255