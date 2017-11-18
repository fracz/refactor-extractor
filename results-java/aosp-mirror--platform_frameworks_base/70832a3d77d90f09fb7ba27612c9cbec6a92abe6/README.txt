commit 70832a3d77d90f09fb7ba27612c9cbec6a92abe6
Author: Jim Miller <jaggies@google.com>
Date:   Mon Jun 13 19:56:32 2011 -0700

    Some tuning for MultiWaveView animations and assets:
    - allow individual chevrons to be specified for (top, bottom, left, right).
    - move ring to pressed position (currently w/o animation)
    - add top/bottom chevron handling and refactor code accordingly.
    - constrain drag handle to the ring

    Change-Id: I859b2d03d8f0397c68b87a8ee15df20d55c9552c