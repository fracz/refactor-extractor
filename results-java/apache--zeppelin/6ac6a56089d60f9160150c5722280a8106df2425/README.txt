commit 6ac6a56089d60f9160150c5722280a8106df2425
Author: Alexander Bezzubov <bzz@apache.org>
Date:   Thu Dec 17 14:48:59 2015 +0900

    ZEPPELIN-510: refactor Integration Test

    This is another approach to solve [ZEPPELIN-510](https://issues.apache.org/jira/browse/ZEPPELIN-510) problem of flacky integration tests.

    It uses [FluentWait](http://selenium.googlecode.com/svn/trunk/docs/api/java/org/openqa/selenium/support/ui/FluentWait.html) to poll every 1s untill 30s timeout.

    Author: Alexander Bezzubov <bzz@apache.org>

    Closes #546 from bzz/fix/zeppelin-510-integration-tests and squashes the following commits:

    53b6491 [Alexander Bezzubov] ZEPPELIN-510: increase MAX browsear delay 30s + poll every 1s