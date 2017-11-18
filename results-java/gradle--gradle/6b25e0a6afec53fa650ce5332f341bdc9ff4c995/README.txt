commit 6b25e0a6afec53fa650ce5332f341bdc9ff4c995
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Sep 12 19:32:46 2011 +0200

    Some refactorings/fixes in the daemon registry area. The updates to the registry (marking idle/busy) are done only when the daemon was not already stopped.