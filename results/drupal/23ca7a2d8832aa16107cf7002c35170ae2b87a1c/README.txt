commit 23ca7a2d8832aa16107cf7002c35170ae2b87a1c
Author: Dries Buytaert <dries@buytaert.net>
Date:   Thu Nov 1 17:04:20 2001 +0000

    - Another batch of updates/improvements:

       + introduced basic node permissions ("create", "delete", "update" and
         "view") at the node level: it's up to the "<$node->type>_module" to
         hide gory details (if any).

       + made the "blog it"-feature in the blog and import module work with
         the new node system, in specific with the new centralized forms.

       + made it possible to update blogs.

       + made the page module work with the new node system.

       + various smaller improvements.