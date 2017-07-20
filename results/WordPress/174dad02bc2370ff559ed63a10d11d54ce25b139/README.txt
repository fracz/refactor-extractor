commit 174dad02bc2370ff559ed63a10d11d54ce25b139
Author: nacin <nacin@1a063a9b-81f0-0310-95a4-ce76da25c4cd>
Date:   Tue Jul 31 17:45:38 2012 +0000

    Optimize get_user_by( 'id', $id ) to return wp_get_current_user() when the current user ID is requested.

    Provides for a major performance improvement by preventing repeated instantiations of WP_User in the capabilities API.

    see #21120.



    git-svn-id: http://core.svn.wordpress.org/trunk@21376 1a063a9b-81f0-0310-95a4-ce76da25c4cd