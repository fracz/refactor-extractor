commit 3d83b0e90bfa0d993c8624fc3032249025d6f269
Author: Jakub Pawlowski <jpawlowski@google.com>
Date:   Fri Nov 4 15:25:57 2016 -0700

    Bluetooth: advertising improvements

    This patch removes isPeripheralModeSupported(), hidden public method
    which is always returning true. It also modify the BluetoothLeAdvertiser
    to be able to use advertising instance with instance id equal 0.

    Bug: 30622771
    Bug: 24099160
    Change-Id: Id31582621dbe56d5c3a8d4ee5cd296af66a5f026