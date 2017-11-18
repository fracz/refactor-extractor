commit 1953f43fa5e925f82fe5873448e4798b7efd3cdd
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Mon Feb 18 09:30:15 2013 +0100

    Speed up kernel tests

    By using ephemeral file system in as many tests as poossible. Some things
    have been changed to accept a FileSystemAbstraction to work with instead
    of File objects directly to make this happen.

    There are some tests still using the disk though, tests which will be
    slightly trickier to change, but given enough time...

    One nice thing that we've gotten rid of are tests that wants to produce a
    non-clean state of a database. They did so by spawning a new process which
    created some data in an embedded db and then crashed. Now with the
    EphemeralFileSystemAbstraction you can take a snapshot of the files before
    shutting down, and use that returned EphemeralFileSystemAbstraction when
    working with the non-clean state. All this in the same process and w/o
    even touching disk.

    This whole change has the most speed improvements on Windows, but I'd
    guess all OSes should see a boost.