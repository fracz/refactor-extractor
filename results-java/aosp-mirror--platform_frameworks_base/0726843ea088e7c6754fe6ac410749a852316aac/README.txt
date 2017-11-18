commit 0726843ea088e7c6754fe6ac410749a852316aac
Author: Svetoslav <svetoslavganov@google.com>
Date:   Wed Feb 13 14:55:19 2013 -0800

    Fixing a NPE in accessibility manager service.

    There was a missing null checks as a result of a recent
    refactoring.

    bug:8185435

    Change-Id: I3a1e256b434755b3a27f609dd2b6aeec31aa9a4f