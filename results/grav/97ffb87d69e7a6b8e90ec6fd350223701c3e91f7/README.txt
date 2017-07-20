commit 97ffb87d69e7a6b8e90ec6fd350223701c3e91f7
Author: Philipp Kitzberger <pk@cron.eu>
Date:   Tue May 17 01:57:08 2016 +0200

    Fix absolute URLs in pipelined CSS (#837)

    * Fix absolute URLs in pipelined CSS

    The way that absolute URLs get excluded during cssRewrite() doesn't cover all possible cases due to a incorrect CSS_URL_REGEX.

    * Improve CSS_URL_REGEX

    Performance improvement by using a back reference. Additionally this makes sure the same kind of quote (single, double, none) is being used.