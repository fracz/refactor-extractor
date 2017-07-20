commit d8a694d8aca61d77370da92595485828e462741c
Author: duck_ <duck_@1a063a9b-81f0-0310-95a4-ce76da25c4cd>
Date:   Fri Feb 10 13:42:15 2012 +0000

    Improve efficiency of make_clickable(). Props mdawaffe. Fixes #16892.

    Not only does this improve general performance, but also helps to prevent
    segfaults caused by malicious input to the regular expression. The regular
    expression is also simplified to help readability and maintenance.


    git-svn-id: http://svn.automattic.com/wordpress/trunk@19899 1a063a9b-81f0-0310-95a4-ce76da25c4cd