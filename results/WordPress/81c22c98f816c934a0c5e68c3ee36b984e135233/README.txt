commit 81c22c98f816c934a0c5e68c3ee36b984e135233
Author: nacin <nacin@1a063a9b-81f0-0310-95a4-ce76da25c4cd>
Date:   Sat Jun 30 11:49:02 2012 +0000

    Don't use switch_to_blog() in wp.getUsersBlogs to improve performance and memory footprint. props mohanjith for initial patch. fixes #20665.

    git-svn-id: http://core.svn.wordpress.org/trunk@21194 1a063a9b-81f0-0310-95a4-ce76da25c4cd