commit 0c820db22c83808fdb06c7cb06aaf13ef4b559a3
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Apr 14 17:47:34 2015 -0700

    Some improvements to battery stats data.

    History now records when wifi data activity starts and "ends"
    based on the triggers we get from the kernel used to determine
    when to collect data.  (Basically the same as the current cell
    data, but of course when it ends is just an arbitrary x seconds
    after the last data traffic.)

    Re-arranged the state bits to make room for this data in the
    right place and move some other things that make more sense to
    have in states2.

    Try to improve overflow handling, so when it happens we allow
    the various bit states to drop to 0 instead of being stuck
    active for an indeterminant amount of time.

    Added recording of the points where we decide we want to
    retrieve new power stats, giving the reason for doing so.
    These are only recorded when full logging is turned on.

    Change-Id: Ic5d216960a07e0eb658731cdfba7f49ad3acf67e