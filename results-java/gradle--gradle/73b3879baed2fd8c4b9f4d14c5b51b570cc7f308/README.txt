commit 73b3879baed2fd8c4b9f4d14c5b51b570cc7f308
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Wed Aug 31 00:22:51 2011 +0200

    Refactorings in the daemon area. Draft refactoring yet, work in progress (will be pushed to branch hence). Main goal was to have single registry file for all daemons. Details:
     - added update method to the caching infrastructure
     - naively (for now) synchronized the DaemonRegistry
     - fixed instability issues with DaemonFunctionalTest