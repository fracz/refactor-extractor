commit 6d03ea26c1e599b2687c2bcfd246e5f2008f8ad0
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Wed Dec 21 20:09:13 2011 +0100

    Exception handling in the daemon server for the first received message...

    The previous behavior led to 'stalled' client when for some reason receiving the first message on the server side has failed. It was exposed by some tooling api use cases and integ tests. This might be also related to recent problems Hans raised. Details:

    -Added exception handling to the daemon server. On exception we dispatch DaemonFailure back to the client. Some refactorings will follow but I need a small changeset I can merge to M7 release branch.
    -Added a coverage that takes full advantage of the embeddedDaemon
    -Unfortunately this problem exists in M5 and M6 which means that I will have to add some extra handling to the tooling api to protect the client in case the target gradle is M5 or M6.