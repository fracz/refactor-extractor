commit 956c25213d771f37f7ed51bd56b0788be05e6603
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Mon Jan 3 05:49:45 2011 +0000

    Various code cleanups and small improvements:
     * Live! widget shows IP for all users except anonymous
     * Widgetize displays full URL to the widget + preview link below widget
     * Live! visitors text change from "Today" to "Last 24 hours" in preview
     * remove data_push feature introduced in r1330 + removing campaign redirect feature since they are not used
     * all errors should now display the Piwik header when applicable (or if a php error, prefixed with a sentence suggesting to submit error in piwik forums)
     * fixing bug with cookie update when a visitor manually converts the same goal in the same second
     * fixing XML output not valid in Chrome (HTML entities not valid, must use XML entities)
     * simplifying + refactoring the truncation code in datatables.js (move from JS to small smarty template - hopefully we can fix this truncation and make it nice soon)
     * removing unnecessary line breaks from translations
     * refactoring duplicate code in renderers

    git-svn-id: http://dev.piwik.org/svn/trunk@3565 59fd770c-687e-43c8-a1e3-f5a4ff64c105