commit a0fad3ba06d0352f640532e69ed3d540b3795535
Author: Selim Cinek <cinek@google.com>
Date:   Fri Sep 19 17:20:05 2014 +0200

    Optimized and improved the status bar performance a lot

    Instead of clearing the Statusbarwindow buffer in the beginning
    we now draw the scrim with mode SRC and therefore a whole screen
    of overdraw is saved!

    Bug: 17287256
    Change-Id: I29f14a2c3d4cb087c422ae6f486d23d7f8ec173b