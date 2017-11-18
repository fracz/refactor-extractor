commit 0a82e703427bad76884396c171a4517e88474d69
Author: tomlu <tomlu@google.com>
Date:   Mon Oct 23 18:16:44 2017 +0200

    Change FileSystem#getDirectoryEntries to return strings of the file/directory names instead of paths.

    This is a small isolated change that can be done ahead of the big refactoring.

    PiperOrigin-RevId: 173124518