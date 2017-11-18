commit 830d18a1bc758d6b4df0d5567eb956274762e524
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Feb 28 18:20:42 2012 +0100

    Added better diagnostics for the daemon disappered problem.

    The improvements are driven by the recent problem - concurrency issue when dispatching messages leading to 'null' message received on the client side. Basically if I had those diagnostics few days ago I would have detected the problem much faster. Details:

    1. The BuildStarted message sent by the daemon contains some diagnostics information, like the location of the daemon log file or the daemon process id. The client can do some useful stuff with it in case the daemon misbehaves.
    2. The daemon client logs extra stuff when daemon supposedly disappears. For example, it includes last X lines of the daemon log file. It is a bit verbose and I wasn't sure if I like it but this is something I was really missing for a long time. Also, disappeared daemon is a fatal error and I believe some verbosity cannot hurt at that point.
    3. Added tail() method to the GFileUtils.