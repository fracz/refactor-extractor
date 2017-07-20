commit 4f08e9d383833aefaa55a3614325bedd27ec2049
Author: Aaron Jorbin <aaron@jorb.in>
Date:   Mon Feb 9 19:27:28 2015 +0000

    Use screen reader text instead of a title attribute in comments_popup_link

    To better understand screen reader text, check out https://make.wordpress.org/accessibility/2015/02/09/hiding-text-for-screen-readers-with-wordpress-core/

    Screen Reader text improves the user experience for screen reader users. It provides additional context for links, document forms and other pieces of a page that may exist visually, but are lost when looking only at the html of a site.  This does change the output of comments_popup_link if you don't pass in values for $zero, $one, $more or $none. Theme authors can and should style <code>.screen-reader-text</code> in ways that are recommended in the above article to hide it visually.

    Props joedolson
    Fixes #26553




    Built from https://develop.svn.wordpress.org/trunk@31388


    git-svn-id: http://core.svn.wordpress.org/trunk@31369 1a063a9b-81f0-0310-95a4-ce76da25c4cd