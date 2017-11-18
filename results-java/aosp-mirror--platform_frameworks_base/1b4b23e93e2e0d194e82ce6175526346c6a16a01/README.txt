commit 1b4b23e93e2e0d194e82ce6175526346c6a16a01
Author: Jorim Jaggi <jjaggi@google.com>
Date:   Tue Nov 15 16:30:12 2016 -0800

    Fix camera not launching immediately from Keyguard

    Seems like a line got lost in one of the many refactors :-)

    Also adds debug statements for UnknownAppVisibilityController.

    Test: Launch camera from lockscreen, make sure no delay.
    Change-Id: Idb49b8dcfd2ce351a62d46d93a917a791d38caa1