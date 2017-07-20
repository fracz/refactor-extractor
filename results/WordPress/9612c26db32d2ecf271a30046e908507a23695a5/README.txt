commit 9612c26db32d2ecf271a30046e908507a23695a5
Author: Pascal Birchler <pascal.birchler@gmail.com>
Date:   Tue Feb 23 22:26:28 2016 +0000

    Users: Introduce `_wp_get_current_user()` for improved backward compatibility.

    This new helper function is used by the pluggable functions `wp_get_current_user()` and `get_currentuserinfo()`, which was previously being called by the former before [36311]. Without it, infinite loops could be caused when plugins implement these functions, as they are now called the other way around.

    Fixes #19615.
    Built from https://develop.svn.wordpress.org/trunk@36651


    git-svn-id: http://core.svn.wordpress.org/trunk@36618 1a063a9b-81f0-0310-95a4-ce76da25c4cd