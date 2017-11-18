commit ea8441e22a4316cb6e78dd8bf461d3e658545b64
Author: Brian Colonna <bcolonna@google.com>
Date:   Wed Apr 25 17:51:49 2012 -0400

    Changes to biometric sensor interface in lockscreen

    This cleans up the biometric sensor interface - the interface between
    lockscreen and Face Unlock.  Not only does it document the interface,
    but it also makes two noteworthy changes to the interface:

    1) Instead of calling mBiometricUnlock.start() with a parameter to
    tell it whether to suppress itself, lockscreen makes all of the
    decisions about whether the biometric unlock should be started or not
    and only calls start() if it should be started.  Passing a parmeter to
    tell a function to not start itself was strange, but it was a
    necessary intermediate step in the process of fixing this interface.

    2) Instead of calling mBiometricUnlock.initializeView() with a top
    view that the biometric unlock should attach to, lockscreen now
    provides the biometric unlock with the actual view it is allowed to
    work in.  This keeps lockscreen in control of where the biometric
    sensor is allowed to display.

    A few things were also cleaned up within the Face Unlock
    implementation of the biometric interface:

    1) Changes needed to match the requirements of the improved biometric
    sensor interface, including moving the functions into an order that
    makes more sense.

    2) The bind() function was only being called from start(), which has
    turned into only a couple of lines of code, so the bind() code has
    been just put inline into the start() function, which mirrors the
    stop() function which has the unbind() code in it.

    3) The showArea() function was really just one line of code with a
    check.  It was being called from two places.  The showArea() code is
    now just written inline in those two places, which makes the code
    much easier to follow.

    4) Renamed occurrences of FaceLock to Face Unlock.

    Change-Id: Ie95ce21dcc77170381af9ce320f2675dfe249741