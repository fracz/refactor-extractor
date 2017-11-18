commit e549a8d62108c7c7dabedbf4e77b9a653781723b
Author: Jorim Jaggi <jjaggi@google.com>
Date:   Mon May 15 02:40:05 2017 +0200

    Optimize latency when unlocking phone

    Latency when unlocking the phone regressed a bit for two reasons:
    - For lockscreen -> app we now have to create a full starting
    window containing the snapshot, while previously this was just
    showing a surface.
    - For lockscreen -> home, we can't use the saved surface anymore
    because currently we don't support snapshotting translucent
    activities. However, in the long term, we want home screen to be
    more involved into transitions anyways, so we'll have to wait for
    the first frame draw anyways.

    However, crystal ball trainee developer Jorim added some
    artificial latency in this transition 3 years ago, because he knew
    that it is going to be an issue at some point so we have some
    headroom to improve! Genius! On a more serious note, it was because
    he didn't understand how to read systraces with binders involved (to
    be fair, there was also no binder tracing).

    Now, we can completely fix the introduces latencies above by
    removing this latency of 100ms, and we are 30-70ms better than
    before! However, this requires a lot of discipline in SystemUI.
    Currently, the callback to dismiss Keyguard takes around 30ms. By
    moving all non-essential binder calls of the main thread or to the
    next frame, we bring this down to 5ms, such that window animation
    and Keyguard animation starts about at the same time.

    Test: Take systrace, unlock phone...profit!

    Fixes: 38294347
    Change-Id: I481fe4ecf358ed09f7142dd9e7ecd290b53c500c
    Merged-In: I3ea672bc2eca47221bc6c9f3d7c56b6899df207d