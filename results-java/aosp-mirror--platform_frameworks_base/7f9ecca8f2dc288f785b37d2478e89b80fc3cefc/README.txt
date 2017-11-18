commit 7f9ecca8f2dc288f785b37d2478e89b80fc3cefc
Author: Matthew Xie <mattx@google.com>
Date:   Fri Jul 15 13:03:58 2011 -0700

    Keep Bluetooth module hot to quickly swith it on/off

    Add BluetoothAdapterStateMachine to maintain a inter state machine other than
    the public BluetoothAdapter states. This is a improvement to BluetoothService
    code. 2 internal state are added, LoadingFirmware and FirmwareLoaded to place
    the Bluetooth module in a ready-to-switch-on state so that it can be quickly
    switched on to have a better user experience
    bug 5021787

    Change-Id: Ia352e88cba509d9e98c900f85e7479f8cee1de5e