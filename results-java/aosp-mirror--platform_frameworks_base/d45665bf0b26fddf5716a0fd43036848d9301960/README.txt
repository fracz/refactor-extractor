commit d45665bf0b26fddf5716a0fd43036848d9301960
Author: Dianne Hackborn <hackbod@google.com>
Date:   Wed Feb 26 12:35:32 2014 -0800

    Collect per-uid mobile radio usage.

    We now compute radio active time per application, by distributing
    the active time across all applications each time the radio goes
    down, weighting it by the number of packets transferred.

    Per-app radio power use is now computed using this radio active
    time.

    This also gives us a new metric "ms per packet", which give an
    idea of how effectively an application is using the radio.  This
    is collected and reported as a new set of stats in the human-
    readable checkin.  (It can be computed from the raw checkin data).

    Also improve sync reporting to include the sync source as used
    in wake locks, not just the component name.

    Change-Id: I0b0185fadd1e47ae749090ed36728ab78ac24c5e