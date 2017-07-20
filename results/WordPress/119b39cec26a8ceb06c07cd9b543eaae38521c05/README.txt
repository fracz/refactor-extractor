commit 119b39cec26a8ceb06c07cd9b543eaae38521c05
Author: markjaquith <markjaquith@1a063a9b-81f0-0310-95a4-ce76da25c4cd>
Date:   Mon May 18 15:11:07 2009 +0000

    deprecate wp_specialchars() in favor of esc_html(). Encode quotes for esc_html() as in esc_attr(), to improve plugin security.

    git-svn-id: http://svn.automattic.com/wordpress/trunk@11380 1a063a9b-81f0-0310-95a4-ce76da25c4cd