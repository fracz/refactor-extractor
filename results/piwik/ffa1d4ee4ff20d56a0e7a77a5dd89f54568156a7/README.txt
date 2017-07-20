commit ffa1d4ee4ff20d56a0e7a77a5dd89f54568156a7
Author: benakamoorthi <benaka.moorthi@gmail.com>
Date:   Fri Feb 3 03:48:57 2012 +0000

    Refs #1465. Added all_integration_tests.php script that invokes all integration tests and nothing else, and refactored all_tests.php. Also added the ability to disable API integration tests & modify the widget testing level via the browser.

    NOTES:
      * Removed widget tests for ExampleRssWidget since it depends on a feedburner URL.



    git-svn-id: http://dev.piwik.org/svn/trunk@5747 59fd770c-687e-43c8-a1e3-f5a4ff64c105