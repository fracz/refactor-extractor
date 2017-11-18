commit 787e22da29a91a1b6c544a4069db557a93c1308c
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Wed Feb 8 15:34:25 2012 +0100

    Some improvements to the daemon logging. Now the daemon infrastructure logs with DEBUG even when the build has started. This way the logs will be much more comprehensive. They won't contain the 'build' DEBUG output unless the client builds with DEBUG.