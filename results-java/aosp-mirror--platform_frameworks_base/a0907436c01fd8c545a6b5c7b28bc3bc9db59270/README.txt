commit a0907436c01fd8c545a6b5c7b28bc3bc9db59270
Author: Jeff Sharkey <jsharkey@android.com>
Date:   Fri Aug 15 10:23:11 2014 -0700

    PackageInstaller API refactoring.

    Switch to using IntentSender for results to give installers easier
    lifecycle management.  Move param and info objects to inner classes.

    Bug: 17008440
    Change-Id: I944cfc580325ccc07acf22e0c681a5542d6abc43