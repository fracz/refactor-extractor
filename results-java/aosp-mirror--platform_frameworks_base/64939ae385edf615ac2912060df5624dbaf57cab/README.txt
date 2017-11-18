commit 64939ae385edf615ac2912060df5624dbaf57cab
Author: Matthew Williams <mjwilliams@google.com>
Date:   Wed Jun 4 09:25:11 2014 -0700

    DO NOT MERGE Redact SyncService stuff from master

    Cherry-pick over from master.
    This changes the API surface area, deleting things we aren't releasing.
    I'll do the internal clean-up at the same time I do the SyncManager
    refactor to sit on top of the TaskManager.
    Bug: 14997851

    Change-Id: Ieebbbcd3324827098e88b36e45e6e82315a51e65