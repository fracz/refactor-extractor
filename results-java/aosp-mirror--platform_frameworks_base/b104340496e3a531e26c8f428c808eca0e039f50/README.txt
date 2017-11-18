commit b104340496e3a531e26c8f428c808eca0e039f50
Author: San Mehat <san@google.com>
Date:   Fri Feb 5 08:26:50 2010 -0800

    Framework: Clean up / Refactor Mount APIs

     - Move android.storage.* -> android.os.storage.* and refactor users
     - Refactor generic shares back to explicit ums enable/disable/isEnabled
     - Remove media insert/removed event callbacks (not ready for Froyo)
     - Remove 'label' from volume state change callbacks
     - Add public API functions for enabling/disabling USB mass storage (permissions enforced
       in MountSevice)
     - Remove some stray un-needed import lines
     - Move android.os.IMountService / android.os.IMountServiceListener -> android.os.storage
     - Improve code comments

    Updated:
      MountService: Add dup state check and move debugging behind a conditional
      UsbStorageActivity: Fix review comments + a TODO
      StorageNotification: Add @Override tags
      StorageManager: Don't use a static Listener list
      MountService: Reduce bloat and fix == where I meant .equals()
      PackageManagerTests: Update for new API

    Signed-off-by: San Mehat <san@google.com>