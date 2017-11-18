commit e0fd693c6098f59004f9e96ad75c058e26c337b0
Author: Nick Pelly <npelly@google.com>
Date:   Wed Jul 11 10:26:13 2012 -0700

    Improve geofencing: throttle location updates with distance to fence.

    Previously any geofence (proximity alert) would turn the GPS on at full rate.
    Now, we modify the GPS interval with the distance to the nearest geofence.
    A speed of 100m/s is assumed to calculate the next GPS update.

    Also
    o Major refactor of geofencing code, to make it easier to continue to improve.
    o Discard proximity alerts when an app is removed.
    o Misc cleanup of nearby code. There are other upcoming changes
      that make this a good time for some house-keeping.

    TODO:
    The new geofencing heuristics are much better than before, but still
    relatively naive. The next steps could be:
    - Improve boundary detection
    - Improve update thottling for large geofences
    - Consider velocity when throttling

    Change-Id: Ie6e23d2cb2b931eba5d2a2fc759543bb96e2f7d0