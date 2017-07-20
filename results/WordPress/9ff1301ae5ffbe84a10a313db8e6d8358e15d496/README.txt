commit 9ff1301ae5ffbe84a10a313db8e6d8358e15d496
Author: Adam Silverstein <adamsilverstein@earthboundhosting.com>
Date:   Fri Apr 14 16:43:44 2017 +0000

    Upload: improve legacy SWFUpload event handlers for current jQuery.

    Fix an issue where legacy JavaScript for SWFUpload still used jQuery's
    deprecated `live` event which no longer works - switch to using `on`.
    This JavaScript is still used by some plugins and themes.

    Props MMDeveloper.
    Fixes #39886.


    Built from https://develop.svn.wordpress.org/trunk@40431


    git-svn-id: http://core.svn.wordpress.org/trunk@40329 1a063a9b-81f0-0310-95a4-ce76da25c4cd