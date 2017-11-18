commit 3839f330820d2d58db7902ac4505c3351ef2353b
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Apr 10 20:31:25 2012 +0200

    Refactored and slightly improved the code around simple communication message that is sent from the starting daemon to the parent process via stream. It's very simple and should be good enough until we need something more robust (i.e. when we start passing the daemon address from the starting daemon).