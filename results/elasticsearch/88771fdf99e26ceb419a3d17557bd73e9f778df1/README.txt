commit 88771fdf99e26ceb419a3d17557bd73e9f778df1
Author: Luca Cavanna <cavannaluca@gmail.com>
Date:   Fri Jan 31 11:49:41 2014 +0100

    [TEST] Added ability to test apis that don't return json

    The last response body gets now always stashed in the REST tests and can be retrieved via `$body`. This implies that not only expected values can be retrieved from the stashed values, but actual values as well.

    Added support for regular expressions to `match` assertion, using `Pattern.COMMENTS` flag for better readability through new custom hamcrest matcher (adopted in do section as well). Functionality added through new feature called `regex` that needs to be mentioned in the skip sections whenever needed till all the runners support it.

    Added also example tests for cat count api