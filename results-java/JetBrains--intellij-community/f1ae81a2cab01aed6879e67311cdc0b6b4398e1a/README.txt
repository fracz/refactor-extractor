commit f1ae81a2cab01aed6879e67311cdc0b6b4398e1a
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Thu Jul 30 17:03:28 2015 +0300

    Use new features of Gitlab REST API to improve search results (support 66401)

    Utilize recently added optional parameters "state" and "order_by" when
    fetching issues from a server to limit query results and make updates
    in "Open Task" faster.