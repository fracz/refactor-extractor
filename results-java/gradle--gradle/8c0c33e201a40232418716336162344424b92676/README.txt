commit 8c0c33e201a40232418716336162344424b92676
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sat Apr 7 12:56:01 2012 +0200

    Fixes to improve stability of the daemon on windows (and improve the stability of tests, too). Details:
    -Parent process initialization (e.g. uninheriting handles for windows) needs to wait for the first spawned child daemon process to close its inputs/outputs. Without that, sometimes the child process were not quite detached in concurrent scenario. It is an awkward change but I didn't figure out a better way to solve this windows awkwardness.
    -Started measuring how long does it take to start off the daemon and placed this information in the logs. This improves debuggability as it gives very good hint on whether the spawned process was detached or not (detached process is started in few seconds max).