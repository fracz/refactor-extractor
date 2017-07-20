commit 2bbd21f543aee73edd0b55bd23623eb1dd6199e9
Author: Gary Pendergast <gary@pento.net>
Date:   Mon Apr 20 04:46:25 2015 +0000

    WPDB: When sanity checking read queries, there are some collations we can skip, for improved performance.

    Props pento, nacin.

    See #21212.


    Built from https://develop.svn.wordpress.org/trunk@32162


    git-svn-id: http://core.svn.wordpress.org/trunk@32137 1a063a9b-81f0-0310-95a4-ce76da25c4cd