commit 9135c3c9676b1022adc4a53ae8013bae90c6a9da
Author: Simon Willnauer <simonw@apache.org>
Date:   Fri Mar 4 17:30:15 2016 +0100

    Only wait for initial state unless we already got a master

    This seems to be an error introduced in refactoring around #16821
    where we now wait 30seconds by default if the node already joined
    a cluster and got a master. This can slow down tests dramatically
    espeically on slow boxes and notebooks.

    Closes #16956