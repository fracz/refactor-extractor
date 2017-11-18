commit c0afeb2b447793c2f22e5ac9c7287e5368f256f9
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri May 25 18:47:42 2012 +0200

    ExecHandle simplifications, related to ibm daemon issue (GRADLE-2321)

    The decision if the ExecHandle forks daemon or not happens at building time. This simplifies a lot the internal design and we can lose the TimeKeepingExecuter. Still refactorings are pending however in current shape we have Gradle working with ibm jvm on windows and we are also past the ibm+daemon issue. Detaching from the daemon does not support timeout or abort yet - it will come next.