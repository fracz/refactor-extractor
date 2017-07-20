commit dc986d9d741062fba6ed04c5ee7d6795c613589c
Author: ayushchd <ayushchd@gmail.com>
Date:   Sun Feb 10 16:34:58 2013 +0530

    CreateDropDatabaseTest refactored to handle AJAX content

    I have refactored the Create/Drop Database test to handle the AJAX
    content, it uses waitForElementPresent() method to wait for an ajax
    response which should be more reliable than sleep(). Next, I have tried
    to use more of cssSelectors rather than xpaths as cssselectors cope up
    well with structural changes on the page. Not yet added to
    AllSeleniumTests.php, waiting for approval.