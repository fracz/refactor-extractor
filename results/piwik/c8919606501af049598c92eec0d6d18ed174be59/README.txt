commit c8919606501af049598c92eec0d6d18ed174be59
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Fri Sep 21 14:37:17 2007 +0000

    Redo the exception handler to display a nice error message with link to piwik.org
    Improved error message when PDO is not enabled
    Now setting a table prefix is not mandatory (prefix can be empty)
    improved UI for website URL
    removed test for php_xml as we dont use yet the utf8_encode/decode


    git-svn-id: http://dev.piwik.org/svn/trunk@108 59fd770c-687e-43c8-a1e3-f5a4ff64c105