commit 541538f099399c02d6bab384a81ea194c09950f6
Author: ayushchd <ayushchd@gmail.com>
Date:   Sun Feb 10 16:40:52 2013 +0530

    Selenium Create Drop DB Test refactored to handle AJAX Content

    I have refactored the Create/Drop Database test to handle the AJAX
    content, it uses waitForElementPresent() method to wait for an ajax
    response which should be more reliable than sleep(). Next, I have tried
    to use more of cssSelectors rather than xpaths as cssselectors cope up
    well with structural changes on the page. Not yet added to
    AllSeleniumTests.php, waiting for approval.