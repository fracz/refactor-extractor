commit 19974be68b637628998d37193cc8ad0c6ccac4d1
Author: Todd Burry <todd@vanillaforums.com>
Date:   Mon Jul 4 16:25:34 2016 -0400

    Refactor the dispatcher

    This is a fairly big refactor, but is really only moving things out of large methods into smaller methods.

    There is a bit of a methodology change too where controllers are discovered before their methods are examined. This mirrors where the dispatcher is going even though it currently doesn’t serve much of a great purpose.