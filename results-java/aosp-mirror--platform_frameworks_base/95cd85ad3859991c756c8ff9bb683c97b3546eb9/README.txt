commit 95cd85ad3859991c756c8ff9bb683c97b3546eb9
Author: Steve McKay <smckay@google.com>
Date:   Thu Feb 4 12:15:22 2016 -0800

    Simplify State initialization w/ better semantics + improved Task security.

    Also, reveal the illusion that we're restoring state in Files and Downloads.
    Also, define a "PairedTask" class that guards calls to task methods
        with checks against isDestroyed. This also let us make all of the tasks
        static, so we get much narrower scope and tasks can even be moved to
        their own files.

    Change-Id: I6a9e8706e1ab1d1f43301e73dd9858a115a6baaf