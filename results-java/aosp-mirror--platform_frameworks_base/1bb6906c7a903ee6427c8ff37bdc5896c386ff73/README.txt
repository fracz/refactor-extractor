commit 1bb6906c7a903ee6427c8ff37bdc5896c386ff73
Author: Christopher Tate <ctate@google.com>
Date:   Fri Feb 19 17:02:12 2010 -0800

    Automatically restore app data at install time

    When an application being installed defines a backupAgent in its manifest, we
    now automatically perform a restore of the latest-known-good data for that app.
    This is defined as "data backed up by this app from this handset, if available;
    otherwise data for this app as it existed when the device was initially
    provisioned."  If neither option exists for the app, no restore action is
    taken.

    The CL involves major changes in the Backup and Package Managers...

    * The Package Manager's act of installing an application has now been split
    into two separate phases, with a data-restore phase optionally occurring
    between these two PM actions.  First, the details of the install are performed
    as usual.  Instead of immediately notifying install observers and issuing the
    install-related broadcasts, the in-process install state is snapshotted and
    the backup manager notified that a restore operation should be attempted.  It
    does this by calling a new API on IBackupManager, passing a token by which it
    identifies its in-progress install state.

    The backup manager then downloads [if possible] the data for the newly-installed
    application and invokes the app's backupAgent to do the restore.  After this
    step, regardless of failure, it then calls back into the Package Manager to
    indicate that the restore phase has been completed, supplying the token that
    was passed in the original notification from the Package Manager.

    The Package Manager then runs the final post-install actions: notifying install
    observers and sending out all the appropriate broadcasts.  It's only at this
    point that the app becomes visible to the Launcher and the rest of the OS.

    ... and a few other bits and pieces...

    * The ApplicationInfo.backupAgentName field has been exposed to the SDK.  This
    can be reverted if there's a reason to do so, but it wasn't clear that this
    info needs to be hidden from 3rd party apps.

    * Debug logging of restore set IDs and operation timeout tokens [used during
    any asynchronous Backup Manager operation] are now consistently in hex for
    readability.

    * We now properly reset our binder identity before calling into the transport
    during restore-set operations.  This fixes a permissions failure when a
    single-app restore was attempted.

    * The 'BackupTest' test app is no longer lumped onto the system partition
    by default.

    Change-Id: If3addefb846791f327e2a221de97c8d5d20ee7b3