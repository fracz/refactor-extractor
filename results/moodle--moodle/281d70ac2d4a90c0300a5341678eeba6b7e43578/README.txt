commit 281d70ac2d4a90c0300a5341678eeba6b7e43578
Author: defacer <defacer>
Date:   Fri Jul 8 03:51:38 2005 +0000

    Merging from STABLE:

    Three improvements:

    1. $nomoodlecookie = true. I just found out about this undocumented(?) hack(?)
       in lib/setup.php which is the perfect way to suppress some warnings. We
       don't need a session cookie for the chatd.

    2. If allow_call_time_pass_reference is Off, socket_getpeername can't work.

    3. Suppress "call time pass by reference is deprecated" messages for
       socket_getpeername, we don't have any choice here.