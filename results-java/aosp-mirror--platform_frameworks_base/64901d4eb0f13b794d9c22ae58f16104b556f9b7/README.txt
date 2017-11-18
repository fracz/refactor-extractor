commit 64901d4eb0f13b794d9c22ae58f16104b556f9b7
Author: Gilles Debunne <debunne@google.com>
Date:   Fri Nov 25 10:23:38 2011 +0100

    Better handles' visibility test

    Checking if the hotspot position is visible instead
    of checking if it is part of the clipped visible rectangle.

    Bug 5638710

    Patch set 2: synchronize static variables you will.
    Patch set 3: renaming and refactored the while loop.
    Patch set 4: synchronize you will (again)
    Patch set 5: parent

    Change-Id: I330510f491c85f910fc61598936113ad07d304e4