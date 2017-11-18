commit 449ce80d1ee51069f630b5b70ab4120b544c0f5f
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu Dec 22 21:27:38 2011 +0100

    (Szczepan/Luke) Fixed the problem with stalling client when daemon used...

    -Now the final listening for remaining input / closing the input stream really happens after we send to the client the daemon failure. That solves Hans' stalled client issue. However, it does not solve the underlying problem although now we will be able to see the actual problem / stack trace because it will be sent back to the client.
    -Added coverage using the embedded daemon which proves very handy already.
    -Needed to refactor some code to enable better testability (DefaultDaemonCommandExecuter)

    (cherry picked from commit fdec22dade1285d72605e7d9889caa501cebff14)