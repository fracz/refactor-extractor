commit d17f4409c320331b395d545a394348c0b42ae8c5
Author: duck_ <duck_@1a063a9b-81f0-0310-95a4-ce76da25c4cd>
Date:   Mon Nov 14 21:32:56 2011 +0000

    Don't pass $post_type as get_page()'s $filter argument. Copy-and-paste error. Removal improves get_page_by_path() performance dramatically by stopping sanitize_post() being called. Fixes #19175.


    git-svn-id: http://svn.automattic.com/wordpress/trunk@19283 1a063a9b-81f0-0310-95a4-ce76da25c4cd