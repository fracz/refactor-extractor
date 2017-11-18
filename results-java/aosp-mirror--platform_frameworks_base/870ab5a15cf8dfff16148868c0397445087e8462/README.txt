commit 870ab5a15cf8dfff16148868c0397445087e8462
Author: Jorim Jaggi <jjaggi@google.com>
Date:   Wed Dec 2 18:37:54 2015 -0800

    Drag up gesture improvements

    - Use current velocity of finger for the animation, makes it feel
    smoother.
    - When flinging downwards, maximize the docked stack again to cancel
    the gesture.

    Change-Id: I284c851e2e418d21e890b9dfe983cfe63300fe10