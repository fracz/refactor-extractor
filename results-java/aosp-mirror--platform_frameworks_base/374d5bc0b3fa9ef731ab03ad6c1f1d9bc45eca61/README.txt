commit 374d5bc0b3fa9ef731ab03ad6c1f1d9bc45eca61
Author: Eric Laurent <elaurent@google.com>
Date:   Fri Oct 16 09:16:26 2009 -0700

    Bluetooth A2DP suspend-resume improvements.

    This change will reduce the occurence rate of A2DP sink suspend resume failures observed in issues 2184627, 2181005 and possibly 2189628.

    More robust suspend/resume logic.
    Use only the suspend request to audio hardware to avoid having two concurent suspend resume control paths.