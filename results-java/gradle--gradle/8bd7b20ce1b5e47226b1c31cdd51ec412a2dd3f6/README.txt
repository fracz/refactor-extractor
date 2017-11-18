commit 8bd7b20ce1b5e47226b1c31cdd51ec412a2dd3f6
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Apr 17 16:41:01 2012 +0200

    Next step in refactoring the ExecHandle stack. Pushed the executer into the ExecHandleRunner, ie. where is needed. We no longer call requestStop() on that executer but it's not needed (i.e. no more work can be scheduled in that executer anyway).