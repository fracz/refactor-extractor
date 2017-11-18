commit d7a087a41bcc563aace5316ce86239976700f292
Author: Robert Muir <rmuir@apache.org>
Date:   Mon Sep 28 11:35:07 2015 -0400

    improve seccomp syscall filtering

    * Add OS X support via "seatbelt" mechanism. This gives consistency across dev and prod, since many devs use OS X.
    * block execveat system call: it may be new, but we should not allow it.