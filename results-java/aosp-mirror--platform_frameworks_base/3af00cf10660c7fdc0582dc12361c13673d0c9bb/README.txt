commit 3af00cf10660c7fdc0582dc12361c13673d0c9bb
Author: Selim Cinek <cinek@google.com>
Date:   Wed May 7 17:27:26 2014 +0200

    Improved notification scroller animation logic

    When an animation was already running, the calculation of the
    new duration was wrong. We are now also starting the animation
    instantly instead of waiting for the next frame.
    Also improved the scrolling performance, which was lagging behind by one frame

    Change-Id: I25d6e6eedf33d94f2f90bdc39d863955c707370c