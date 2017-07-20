commit 5b2c145a4deb04ecff4196ba482be4e978c780cd
Author: nacin <nacin@1a063a9b-81f0-0310-95a4-ce76da25c4cd>
Date:   Fri Jul 20 04:21:42 2012 +0000

    Improve the performance of WP_Object_Cache's _exists() method.

    Results showed a performance improvement on one admin screen of 90ms (~2%).

    fixes #21320. see #20004.



    git-svn-id: http://core.svn.wordpress.org/trunk@21285 1a063a9b-81f0-0310-95a4-ce76da25c4cd