commit 518ff1576bc70c305da11bdfaebfee62946ff632
Author: Adam Silverstein <adamsilverstein@earthboundhosting.com>
Date:   Fri Feb 17 20:54:44 2017 +0000

    REST API: JavaScript client - improve route discovery for custom namespaces.

    Fix parsing of custom namespace routes. Transform class names, removing dashes and capitalizing each word/route part so a route path of `widgets/recent-posts` becomes a collection with the name `WidgetsRecentPosts`. Correct parent route part when routes are longer than expected, reversing parse direction.

    Props westonruter, jazbek.
    Fixes #39561.

    Built from https://develop.svn.wordpress.org/trunk@40074


    git-svn-id: http://core.svn.wordpress.org/trunk@40011 1a063a9b-81f0-0310-95a4-ce76da25c4cd