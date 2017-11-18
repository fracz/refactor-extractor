commit a44a1e77ef435942b2ff60e9e60d3a64b43d775b
Author: Jaikumar Ganesh <jaikumar@google.com>
Date:   Fri Feb 11 12:40:44 2011 -0800

    Distinguish between NAP and PAN role disconnections

    Distinguish between NAP and PANU disconnect in BT tethering
    and call appropriate functions. We were never disconnecting
    NAP role devices.

    ToDo: BluetoothService needs to be refactored, its become too big
    1) BluetoothAdapter and BluetoothDevice properties need to be moved to separate
    classes.
    2) BluetoothPanProfile and BluetoothInputDeviceProfile which are handled
    by BluetoothService need to be moved to a separate file.
    3) Integrate PAN to the profile state machine.

    Change-Id: I32a8d274f38c78931434bd9738c8f6570ba89fcf