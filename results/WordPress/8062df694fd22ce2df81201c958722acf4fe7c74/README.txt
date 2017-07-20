commit 8062df694fd22ce2df81201c958722acf4fe7c74
Author: Felix Arntz <felix-arntz@leaves-and-love.net>
Date:   Fri Jan 20 16:52:39 2017 +0000

    Multisite: Correct and improve i18n strings in `wp-signup.php`.

    The strings addressing the network administrator in `wp-signup.php` were still using the old terminology of blogs and sites. Furthermore concatenation of the strings has been removed to make them i18n-friendly.

    Props jignesh.nakrani, SergeyBiryukov.
    Fixes #39611.

    Built from https://develop.svn.wordpress.org/trunk@39929


    git-svn-id: http://core.svn.wordpress.org/trunk@39866 1a063a9b-81f0-0310-95a4-ce76da25c4cd