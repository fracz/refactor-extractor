commit bf174cf292d25520c260a4a741ce31d68d41cd9b
Author: Christian Flothmann <christian.flothmann@gmail.com>
Date:   Sat Oct 18 21:00:49 2014 +0200

    [FrameworkBundle] improve server commands feedback

    * display a message when `server:start` is executed and the PCNTL
      extension is not loaded
    * print instructions about how to terminate the `server:run` command