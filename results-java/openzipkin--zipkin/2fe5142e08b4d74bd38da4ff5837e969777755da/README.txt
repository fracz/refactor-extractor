commit 2fe5142e08b4d74bd38da4ff5837e969777755da
Author: Adrian Cole <adriancole@users.noreply.github.com>
Date:   Fri Sep 8 08:10:32 2017 +0200

    Decouples v2 types from v1 types, in preparation of a new package (#1726)

    This moves all the code needed for v2 types into zipkin.internal.v2
    without any references to code outside the package. To reduce
    duplication, "v1" code can reference v2 types (as they need to in order
    to covert for example). Once this is in, we can consider refactoring out
    a v2 module.