commit c40c79ac66d8162880e0ef76c98f44bc40797bbd
Author: Selim Cinek <cinek@google.com>
Date:   Tue Nov 8 09:52:52 2016 -0800

    Added the possibility to animate X and refactoring

    A viewstate can now animate its X value.
    This also refactors the animation logic such that
    an application when an animation is running will
    update the existing animation nicely.

    Test: manual, observe views animating in X
    Bug: 32437839
    Change-Id: Ic091d87e530af793281ca3f2b1e9370ff5dac236