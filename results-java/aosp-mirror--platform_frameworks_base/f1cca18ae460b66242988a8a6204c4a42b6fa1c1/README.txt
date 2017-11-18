commit f1cca18ae460b66242988a8a6204c4a42b6fa1c1
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu Aug 1 10:50:28 2013 -0700

    Try to improve PSS collection.

    The goal is to collect PSS data from processes when they
    are in as stable a state as possible.  We now have tables
    for the durations to use for PSS collection based on
    process state, and scheduling of collection is all driven
    by process state changes.

    Change-Id: I95e076f6971b9c093118e53dc8240fb3f5bad3b2