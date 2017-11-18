commit 7caf38a35b7ac14e2b6494c14fc7bbcb334e8a42
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu Sep 8 23:02:56 2011 +0200

    Refactoring & hunting down some race conditions in the daemon. Details:
    -Refactored some Daemon code. Daemon now is busy only when actually building something. Updated the docs for the DaemonRegistry
    -Improved the daemon functional test. Now it actually tells the daemon process to perform dummy builds which gives much nicer coverage. OTOH, the test might be a bit more fragile to timeouts.
    -Introduced a very simple command to tell the Daemon to pretend to build something for specified amount of time. This enables much better functional testing.
    -Since the daemon code was refactored I could remove a hack from the PersistentDaemonRegistry