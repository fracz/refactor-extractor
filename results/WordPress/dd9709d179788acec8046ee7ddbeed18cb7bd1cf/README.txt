commit dd9709d179788acec8046ee7ddbeed18cb7bd1cf
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Thu Oct 1 03:11:30 2015 +0000

    Simplify pagination logic in `comments_template()`.

    [34561] "fixed" the problem of newest-first comments showing fewer than
    'per_page' comments on the post permalink when the total number of comments
    was not divisible by 'per_page'. See #29462. But this fix caused numerous
    other problems. First, comment pages reported by `get_page_of_comment()`
    (which expects comment pages to be filled oldest-first) were no longer correct.
    Second, and more seriously, the new logic caused comments to be shifted
    between pages, making their permalinks non-permanent.

    The current changeset reverts the changed behavior. In order to preserve the
    performance improvements introduced in [34561], an additional query must be
    performed when 'default_comments_page=newest' and 'cpage=0' (ie, you're viewing
    the post permalink). A nice side effect of this revert is that we no longer
    need the hacks required to determine proper comment pagination, introduced in
    [34561].

    See #8071. See #34073.
    Built from https://develop.svn.wordpress.org/trunk@34729


    git-svn-id: http://core.svn.wordpress.org/trunk@34693 1a063a9b-81f0-0310-95a4-ce76da25c4cd