commit 2b111747854237689222847fe2fd45e847bc58f6
Author: duck_ <duck_@1a063a9b-81f0-0310-95a4-ce76da25c4cd>
Date:   Tue Sep 27 10:22:58 2011 +0000

    _wp_menu_output() speed up and clean up. Props dragoonis. Fixes #13662.

    Attempt file_exists() last and cache an array index lookup for speed up. Whitespace and appropriate parentheses for readability.


    git-svn-id: http://svn.automattic.com/wordpress/trunk@18792 1a063a9b-81f0-0310-95a4-ce76da25c4cd