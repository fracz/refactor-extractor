commit 83fbd4e36b11240627503ce8dea13439db63c9e3
Author: nacin <nacin@1a063a9b-81f0-0310-95a4-ce76da25c4cd>
Date:   Fri Nov 12 10:15:18 2010 +0000

    Memory usage and execution improvements in wpdb. Store and work with resources directly, rather than full copies of results. Plugins which incorrectly used wpdb->last_result (a private property) will need to shift to wpdb->get_results() with no \$query. Magic getter is introduced for back compat when using PHP5. props joelhardi, fixes #12257.


    git-svn-id: http://svn.automattic.com/wordpress/trunk@16320 1a063a9b-81f0-0310-95a4-ce76da25c4cd