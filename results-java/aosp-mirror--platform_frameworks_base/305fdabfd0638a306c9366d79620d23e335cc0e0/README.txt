commit 305fdabfd0638a306c9366d79620d23e335cc0e0
Author: Andre Eisenbach <eisenbach@google.com>
Date:   Wed Nov 11 21:43:26 2015 -0800

    Move Bluetooth startup to PHASE_ACTIVITY_MANAGER_READY

    For some reason BluetoothManagerService recently has been unable to bind
    to the BluetoothService, causing timeouts, which in turn could lead to
    Bluetooth not working immediately after boot.

    While refactoring that code, the "get name and address" code was also
    removed to make the startup sequence more transparent and easier to
    debug.

    Bug: 25597150
    Change-Id: I62ffe4589a76ea8a6db75e3c7599f149db677ea3