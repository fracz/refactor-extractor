commit b0994bb970cb101514f21affae21c7da64b518c6
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Apr 10 19:52:11 2012 +0200

    Improved the error reporting the daemon presents. This helped me a lot earlier to hunt down the daemon issues. Details:

    -When the daemon starts it sends back a message via standard stream to the parent process. Now this message also contains useful daemon diagnostics, specifically the daemon log file and pid. We can use this log file later in case some exception happens. Later on I will use those diagnostics in other places, too.
    -Refactored the initialization of the parent process so that the spawning operation can return value. Therefore Factory interface is used.
    -For now, we still loop through the all daemons in the daemon connector but I'm getting closer to get rid of it.
    -This refactoring is a work in progress. It needs tidy-up. I needed to get this pushed to incrementally improve the daemon.