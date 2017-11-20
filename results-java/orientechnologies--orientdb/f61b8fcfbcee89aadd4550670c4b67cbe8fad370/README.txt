commit f61b8fcfbcee89aadd4550670c4b67cbe8fad370
Author: lvca <l.garulli@gmail.com>
Date:   Thu Apr 14 13:42:17 2016 +0200

    Distributed: fixed remaining issues

    * fixed problem with transaction rollback in case quorum is not reached
    * fixed problem with coordinator election
    * fixed create/drop db in cluster
    * improved logging