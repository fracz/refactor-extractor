commit 57ff834702f75760e2d611590e44c13f7b3c580d
Author: Klaus Aehlig <aehlig@google.com>
Date:   Fri Jul 14 12:25:11 2017 +0200

    experimental UI: keep console updated till the end

    For commands that do not send a BuildFinishedEvent, we normally use the
    NoBuildEvent to determine the end of the build and hence the moment
    where the UI should not any more interfere with the output. For some
    requests, like fetch, however, we should continue to report progress
    till the very end (as there is no output to interfere with). Do so,
    and also be sure that the experimental UI also reports downloads if
    not explicitly in a loading or analysis phase.

    While there, also group digits in the number of downloaded bytes, to
    increase readability.

    Change-Id: I31efeee5bdb1d29b2ecf842acb3e383e297707f8
    PiperOrigin-RevId: 161935456