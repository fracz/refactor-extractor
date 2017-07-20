commit 4ee739e0f859ffe60d9a857acedebfc5dbf7081c
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Thu Nov 22 02:18:54 2012 +0000

    Site search improvements
     * Support &kwd[]= notation fixes #3454
     * Support random text case fixes #3539
     * Fixes umlauts regression with non utf8 encoding: Fixes #3450
     * Adding setPageCharset() method to Tracking API FIxes #3565

    git-svn-id: http://dev.piwik.org/svn/trunk@7521 59fd770c-687e-43c8-a1e3-f5a4ff64c105