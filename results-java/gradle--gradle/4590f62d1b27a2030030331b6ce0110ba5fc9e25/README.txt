commit 4590f62d1b27a2030030331b6ce0110ba5fc9e25
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sat Dec 17 11:58:39 2011 +0100

    Enabled daemon debug logging when using tooling api...

    -Enabled verbose logging in our tooling api fixture so it is turn on for all tooling api integ tests. Very useful for debugging tooling api/deamon problems.
    -Added setVerboseLogging() method to the DefaultGradleConnector. This means it is not a public API yet, however if someone really needs it he can still use it.
    -The 'lenient' mode for protocol objects is needed more and more. Some refactoring in this area is pending.