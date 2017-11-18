commit 8915430e4adba244f52b225489bedd0dec354587
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Apr 17 18:26:05 2012 +0200

    Next step in refactoring of the ExecHandle stack.

    I tried to balance out the refactorings with carefulness not to break this core functionality. I think we'll continue to refactor the ExecHandle slowly into the shape we want. Details:

    -Removed the ExecHandle.startDaemon() method because now we decide in the ExecHandleBuilder if the process should be a daemon or not.
    -If the process is daemon waitForFinish() method waits until the process is demonized (ie. streams drained and closed).

    Pending: more coverage, some cleanup regarding the 'demonized' result.