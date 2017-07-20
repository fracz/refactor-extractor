commit 8eb703d33a129d56dbedcc7cf98d0b9fe11998da
Author: nacin <nacin@1a063a9b-81f0-0310-95a4-ce76da25c4cd>
Date:   Tue Mar 20 19:06:43 2012 +0000

    Don't suggest only add_theme_support('custom-background') -- suggest it with $args even if it wasn't called with any arguments. In particular, default-color should be used by themes as a good user experience improvement. see #20249.



    git-svn-id: http://svn.automattic.com/wordpress/trunk@20220 1a063a9b-81f0-0310-95a4-ce76da25c4cd