commit 43c4f539f47683164d3805200a0566ec5e63c199
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sat Apr 28 11:36:48 2012 +0200

    Daemon works now with ibm jvm on windows.

    We might find a better solution down the road. The changes that currently made it possible to use daemon with ibm jvm and winXp:
    -the parent process no longer waits for the stream EOF. Instead we wait for a certain greeting message from the daemon. Receiving the message makes the parent assume the daemon has started.
    -if the daemon dies prematurely (for example: invalid jvm opts, java cannot be started, etc.) the parent still consumes all daemon output. This way we can present a decent message with exact daemon failure reason.
    -ExecHandle stack allows to configure the way process' streams are handled.

     Pending:
     -review this approach for safely starting the daemon
     -CI coverage ibm+windows!
     -wait for daemon greeting with timeout!
     -some refactorings