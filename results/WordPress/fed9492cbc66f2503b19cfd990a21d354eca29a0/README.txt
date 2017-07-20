commit fed9492cbc66f2503b19cfd990a21d354eca29a0
Author: nacin <nacin@1a063a9b-81f0-0310-95a4-ce76da25c4cd>
Date:   Tue Jul 31 17:47:08 2012 +0000

    Use get_userdata() rather than new WP_User in is_super_admin(), to take advantage of the performance improvements in [21376]. see #21120.



    git-svn-id: http://core.svn.wordpress.org/trunk@21377 1a063a9b-81f0-0310-95a4-ce76da25c4cd