commit a47d153647d8750b667942ad80e7b905ea98c68b
Author: Eric Laurent <elaurent@google.com>
Date:   Fri Oct 16 09:16:26 2009 -0700

    do not merge - Fix for issue 2184627 cherry picked from eclair-mr2

    Bluetooth A2DP suspend-resume improvements.

    This change will reduce the occurence rate of A2DP sink suspend resume failures observed in issues 2184627, 2181005 and possibly 2189628.

    More robust suspend/resume logic.
    Use only the suspend request to audio hardware to avoid having two concurent suspend resume control paths.