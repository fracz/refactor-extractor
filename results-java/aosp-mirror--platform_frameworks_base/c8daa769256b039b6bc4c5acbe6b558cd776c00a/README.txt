commit c8daa769256b039b6bc4c5acbe6b558cd776c00a
Author: Christopher Tate <ctate@google.com>
Date:   Mon Jul 6 19:04:57 2009 -0700

    BackupManager wrapper class improvements

    + Now rechecks the cached IBinder each time the wrapper is used, and if it's
    still null (i.e. the BackupManager was constructed before the system service
    came up) it's refetched.  This lets even system code cache a single
    BackupManager instance and just keep making calls through it without worrying
    about interactions with the life cycle of the backup service.

    + Added a static dataChanged(packageName) method as a convenience for code that
    needs to indicate that some other package needs a backup pass.  This is useful
    even for third party code in the case of multiple packages in a shared-uid
    situation.