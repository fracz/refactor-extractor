commit e2f515825b0f6d842d25ff7d96ef46795cac0e98
Author: Roman Shevchenko <roman.shevchenko@jetbrains.com>
Date:   Fri Sep 29 19:59:37 2017 +0200

    [java] Javadoc runner improvements

    - shuffles conditions to perform simple checks before more expensive ones
    - puts classpath into an @argfile
    - adds a filter for viewing a contents an @argfile
    - register an @argfile for deletion
    - formatting, unused properties, etc.