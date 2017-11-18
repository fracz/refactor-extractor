commit e219a24a30960ab5c686dd09197b5e22626e1fe0
Author: Philipp Wollermann <philwo@google.com>
Date:   Thu Aug 18 14:39:37 2016 +0000

    Implement the first stage of Bazel's "Sandbox 2.0" for Linux.

    This has the following improvements upon the older one:
    - Uses PID namespaces, PR_SET_PDEATHSIG and a number of other tricks for
      further process isolation and 100% reliable killing of child processes.
    - Uses clone() instead of unshare() to work around a Linux kernel bug that
      made creating a sandbox unreliable.
    - Instead of mounting a hardcoded list of paths + whatever you add with
      --sandbox_add_path, this sandbox instead mounts all of /, except for what
      you make inaccessible via --sandbox_block_path. This should solve the
      majority of "Sandboxing breaks my build, because my compiler is installed
      in /opt or /usr/local" issues that users have seen.
    - Instead of doing magic with bind mounts, we create a separate execroot for
      each process containing symlinks to the input files. This is simpler and
      gives more predictable performance.
    - Actually makes everything except the working directory read-only
      (fixes #1364). This means that a running process can no longer accidentally
      modify your source code (yay!).
    - Prevents a number of additional "attacks" or leaks, like accidentally
      inheriting file handles from the parent.
    - Simpler command-line interface.
    - We can provide the same semantics in a Mac OS X sandbox, which will come in
      a separate code review from yueg@.

    It has the following caveats / known issues:
    - The "fallback to /bin/bash on error" feature is gone, but now that the
      sandbox mounts everything by default, the main use-case for this is no
      longer needed.

    The following improvements are planned:
    - Use a FUSE filesystem if possible for the new execroot, instead of creating
      symlinks.
    - Mount a base image instead of "/".

    FAQ:
    Q: Why is mounting all of "/" okay, doesn't this make the whole sandbox
       useless?
    A: This is still a reasonable behavior, because the sandbox never tried to
       isolate your build from the operating system it runs in. Instead it is
       supposed to protect your data from a test running "rm -rf $HOME" and to
       make it difficult / impossible for actions to use input files that are not
       declared dependencies. For even more isolation the sandbox will support
       mounting a base image as its root in a future version (similar to Docker
       images).

    Q: Let's say my process-specific execroot contains a symlink to an input file
       "good.h", can't the process just resolve the symlink, strip off the file
       name and then look around in the workspace?
    A: Yes. Unfortunately we could not find any way on Linux to make a file appear
       in a different directory with *all* of the semantics we would like. The
       options investigated were:
       1) Copying input files, which is much too slow.
       2) Hard linking input files, which is fast, but doesn't work cross-
          filesystems and it's also not possible to make them read-only.
       3) Bind mounts, which don't scale once you're up in the thousands of input
          files (across all actions) - it seems like the kernel has some
          non-linear performance behavior when the mount table grows too much,
          resulting in the mount syscall taking more time the more mounts you
          have.
       4) FUSE filesystem, good in theory, but wasn't ready for the first
          iteration.

    RELNOTES: New sandboxing implementation for Linux in which all actions run in a separate execroot that contains input files as symlinks back to the originals in the workspace. The running action now has read-write access to its execroot and /tmp only and can no longer write in arbitrary other places in the file system.

    --
    Change-Id: Ic91386fc92f8eef727ed6d22e6bd0f357d145063
    Reviewed-on: https://bazel-review.googlesource.com/#/c/4053
    MOS_MIGRATED_REVID=130638204