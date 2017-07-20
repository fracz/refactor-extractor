commit f17d168a0f72211a9bfd9d3fa680713069871bb6
Author: Gary Pendergast <gary@pento.net>
Date:   Thu Nov 20 01:46:24 2014 +0000

    WPDB: Force `STRICT_ALL_TABLES` to be enabled as soon as we connect to the MySQL server.

    This improves data integrity when inserting and updating rows in the database, particularly when trying to insert emoji into posts stored with character sets that don't support emoji.

    See #21212.

    Built from https://develop.svn.wordpress.org/trunk@30400


    git-svn-id: http://core.svn.wordpress.org/trunk@30396 1a063a9b-81f0-0310-95a4-ce76da25c4cd