commit 4a8dddbf18fedb33bb2c725db489669a141e1d73
Author: Craig Mautner <cmautner@google.com>
Date:   Wed Aug 13 10:49:26 2014 -0700

    Clean up app following death when creating service

    If an app has died, run through the cleanup before relaunching its
    service.

    Also a little simplifying refactor.

    Fixes bug 16979752.

    Change-Id: I376cbef2ea00fc626588386317f092cc6dea0bdc