commit 14055691a3d59a95eac01d22aa57e4cb640e60bd
Author: Jesse Vincent <jesse@fsck.com>
Date:   Sat Nov 13 21:40:56 2010 +0000

    Merge branch 'mail-on-sd'

    * mail-on-sd: (40 commits)
      Added more comments to explain how the locking mecanism works for LocalStore
      Fixed wrong method being called during experimental provider initialization (since provider isn't enabled, that didn't harm)
      Add more comments about how the various StorageProviders work and how they're enabled
      find src/com/fsck/ -name \*.java|xargs astyle --style=ansi --mode=java --indent-switches --indent=spaces=4 --convert-tabs
      French localization for storage related settings
      Remove unused SD card strings (replaced with storage indirection)
      Merge mail-on-sd branch from trunk
      Reset mail service on storage mount (even if no account uses the storage, to be improved)
      find src/com/fsck/ -name \*.java|xargs astyle --style=ansi --mode=java --indent-switches --indent=spaces=4 --convert-tabs
      Migraion -> Migration
      move the Storage location preference into preferences rather than the wizard.
      Made LocalStore log less verbose Added @Override compile checks
      Added ACTION_SHUTDOWN broadcast receiver to properly initiate shutdown sequence (not yet implemented) and cancel any scheduled Intent
      Be more consistent about which SQLiteDatabase variable is used (from instance variable to argument variable) to make code more refactoring-friendly (class is already big, code extraction should be easier if not referencing the instance variable).
      Added transaction timing logging
      Factorised storage lock/transaction handling code for regular operations.
      Use DB transactions to batch modifications (makes code more robust / could improve performances)
      Merge mail-on-sd branch from trunk
      Update issue 888 Added DB close on unmount / DB open on mount
      Update issue 888 Back to account list when underlying storage not available/unmounting in MessageView / MessageList
      ...