commit 2ea7c06065c2d08b2b1b5f589db9ed811ed14741
Author: Stephane Landelle <slandelle@gatling.io>
Date:   Mon Apr 24 20:54:13 2017 +0200

    Improve remote address handling in TimeoutHolder

    Motivation:

    d86d481d754910ffede35ca3358d51c0c32fb7e9 reduced GC with only
    generating String from InetSocketAddress.

    There’s still room for improvement:
    * Less String concatenations when generating message
    * Stop reporting « not-connected » when timeout happens before channel
    gets connected

    Modifications:

     * Use a pooled StringBuilder to concatenate timeout message part
    (still, IP String is not optimized as algorithm is very complex)
    * Set an unresolved InetSocketAddress when creating TimeoutHolder

    Result:

    More informative timeout message and less allocations