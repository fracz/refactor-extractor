commit 598c90d0a24140ef81042efd3fd4aac9d48ec5ed
Author: Klaus Aehlig <aehlig@google.com>
Date:   Fri May 5 12:25:04 2017 +0200

    experimental_ui: improve --show_timestamp handling

    Move the position of the timestamp to the beginning of the line to
    have a more readable log. Also, show the timestamp for progress as
    well. While there, reduce timestamp to second precision, to reduce
    noise.

    Change-Id: Ibfa6caca2e0d207f54e3660bccbf894bba3c5ae3
    PiperOrigin-RevId: 155181731