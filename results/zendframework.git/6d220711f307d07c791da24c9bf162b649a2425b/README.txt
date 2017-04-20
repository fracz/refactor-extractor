commit 6d220711f307d07c791da24c9bf162b649a2425b
Author: Intiilapa <intiilapa-zf@ssji.net>
Date:   Tue Dec 13 22:50:28 2011 +0100

    Logger refactor: rewrote writers

    Rename method from _write() to doWrite().
    Firebug writer is broken right now. It needs to be rewrite for the new request/response HTTP objects.