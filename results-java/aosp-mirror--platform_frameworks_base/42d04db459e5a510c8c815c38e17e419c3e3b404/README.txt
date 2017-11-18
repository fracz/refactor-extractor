commit 42d04db459e5a510c8c815c38e17e419c3e3b404
Author: Craig Mautner <cmautner@google.com>
Date:   Thu Nov 6 12:13:23 2014 -0800

    More fixes for keyguard animations.

    Add a state machine for calling comeOutOfSleepIfNeededLocked() so
    that it is only called after the lockscreen has started dismissing
    but not before resumeTopActivityLocked(). Also keep
    resumeTopActivityLocked() from being called from
    comeOutOfSleepIfNeededLocked() recursively.

    Have starting windows count towards notifying the keyguard that a
    window has been drawn.

    Do not update wallpaper animations based on their not being included
    in the windows being animated if there are no windows being animated.

    And always improve logging.

    Fixes bug 15991916.

    Change-Id: I0d21c5337f0e89d9eacc8dab2cdaa52fec43ac0b