commit 9a62c9cd6585656f4e29ba971b1f88a510d674bd
Author: Jake Hamby <jhamby@google.com>
Date:   Thu Dec 9 14:47:57 2010 -0800

    Refactor android.server.BluetoothService classes.

    Several cleanups to BluetoothService:
    - Move BluetoothService.BondState inner class to top level.
    - Extract adapter and remote device properties cache management
      logic into two new classes: BluetoothAdapterProperties and
      BluetoothDeviceProperties.
    - Add getter methods for other classes in the package to access
      the new properties cache objects for multi-part operations.
    - Inline log() method in BluetoothService and selected the
      appropriate Log method (Log.d, Log.w, Log.e) for each message.
    - Refactor dump() method into smaller sized pieces.
    - Clean up logic of updateCountersAndCheckForConnectionStateChange()
      method for better readability.
    - Change sendConnectionStateChange() to return instead of sending
      an intent if the current or previous state values are invalid.
      Previously the code sent an intent with -1 for the invalid state.
    - Added Javadoc comments to document the methods that are called from
      native Bluez code.
    - Fixed some typos and code style issues.

    Original Change by: Jake Hamby
    Modified by: Jaikumar Ganesh

    Change-Id: I76ebac00ecd29566dcb2d1ad3e7a486b7530ce24