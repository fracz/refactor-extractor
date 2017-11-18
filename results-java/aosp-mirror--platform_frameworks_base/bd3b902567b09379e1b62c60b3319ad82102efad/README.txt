commit bd3b902567b09379e1b62c60b3319ad82102efad
Author: Jeff Sharkey <jsharkey@android.com>
Date:   Tue Aug 20 15:20:04 2013 -0700

    Add CancellationSignal support to file operations.

    Since ContentProvider file operations can end up doing substantial
    network I/O before returning the file, allow clients to cancel their
    file requests with CancellationSignal.

    Ideally this would only be needed for openFile(), but ContentResolver
    heavily relies on openAssetFile() and openTypedAssetFile() for common
    cases.

    Also improve documentation to mention reliable ParcelFileDescriptors
    and encourage developers to move away from "rw" combination modes,
    since they restrict provider flexibility.  Mention more about places
    where pipes or socket pairs could be returned.

    Improve DocumentsContract documentation.

    Bug: 10329944
    Change-Id: I49b2825ea433eb051624c4da3b77612fe3ffc99c