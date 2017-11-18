commit 308bf6468a426b7b2a6fa753086953a014d9f07a
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon May 7 08:36:23 2012 +0200

    First step in adding capability of timeout to the exec handle stack.

    ExecHandleRunner does not block when waiting for streams or process completion. Instead it passes a runnable instance to TimeKeepingExecuter who runs it but also is able to execute a certain 'onTimeout' callback.

    Pending: peer review, refactorings, integ test coverage for daemons that start and hang.