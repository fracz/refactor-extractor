commit 5dd4f9c9db6325e5599f3951764345c4f1c5c5ed
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Jan 13 15:17:07 2012 +0100

    Minor refactoring before tackling GRADLE-1799 (no decent feedback when daemon fails to start)...

    I'm not starting to work on it just now but I wanted to document some of the code investigation I did. Reworked the loop a little bit because it didn't make sense to ask for the connection immediately after starting the daemon (it surely hasn't started yet). Fixed the @Ignore import.