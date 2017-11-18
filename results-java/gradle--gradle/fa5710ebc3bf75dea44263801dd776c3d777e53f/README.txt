commit fa5710ebc3bf75dea44263801dd776c3d777e53f
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Feb 28 11:51:54 2012 +0100

    Disappeared deamon problem, SocketConnection thread safety.

    1. This time, the problem is because SocketConnection is not thread safe and we assume that it is. The issue is exposed byt the ProjectLoadingIntegrationTest (need to be ran multiple times).
    2. I do have a coverage of that but I'm not happy with it so I'd like to improve it before pushing. I'd like to push the fix anyway to make the CI soak it.
    3. The fix is not complete. I've only synchronized the dispatch method because it seems to be most problematic. However, some more analysis nad refactoring is needed in the SocketConnection area to make sure we cover all potential concurrency issues. Synchronizing the Connection object needs some designing and analysis because some methods are blocking, some not and we need make some decisions how do we like them to behave in concurrent scenario.