commit 357d99c61d1c97199ca421408b3e26566e879f2a
Author: Dianne Hackborn <hackbod@google.com>
Date:   Wed Jun 20 18:37:16 2012 -0700

    DO NOT MERGE Fix issue #6697105: App launching sometimes has random pauses

    In the course of the window manager refactoring into a separate
    layout state, we introduced a bad interaction between the two
    sides of the world.  This resulting in multiple hops needed between
    the two sides after an application has said it is finished drawing
    its window, until the window/app transition is actually started.
    Especially since these hops require going through the anim side
    which is vsynced (so will delay its operation until the next frame),
    this could introduce a notable delay until the window is first shown.

    Fix this by re-arranging the code to make one straight path from
    when a window reports it is shown to us starting the app transition
    that is waiting for it.  This change also includes various improvements
    to debugging code that was done while working on it.

    Change-Id: I7883674052da1a58df89cd1d9b8d754843cdd3db