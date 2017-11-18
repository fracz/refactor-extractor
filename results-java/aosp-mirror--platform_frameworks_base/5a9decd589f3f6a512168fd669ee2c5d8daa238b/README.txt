commit 5a9decd589f3f6a512168fd669ee2c5d8daa238b
Author: Matthew Williams <mjwilliams@google.com>
Date:   Wed Jun 4 09:25:11 2014 -0700

    Redact SyncService stuff from master

    This changes the API surface area, deleting things we aren't releasing.
    I'll do the internal clean-up at the same time I do the SyncManager
    refactor to sit on top of the TaskManager.
    Bug: 14997851
    Change-Id: Ibefbb246f0e98d3159399151744279902468a23c