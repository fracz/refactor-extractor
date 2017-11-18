commit 1fadab5c36445bb9f0997904dbce44f8e234f847
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu Apr 14 17:57:33 2011 -0700

    More battery stats improvements.

    We now write to the parcel using deltas.  For common situations,
    it only takes 4 bytes to write a delta (new command, time delta,
    significant state changes, flags indicating additional state that
    follows).

    Increasing the buffer size to 128K, this give us 32,768 samples
    if they all fit in the smallest delta.  A device that is doing
    something every minute (like acquiring a wake lock or doing a
    wifi scan) for our max target battery life of 30 days would
    generate 43,200 samples.

    Also some turning to the maximum time between samples at which
    we decide to completely collapse two samples.

    Change-Id: I074a698d27ccf9389f9585abfc983af2f5ba7a54