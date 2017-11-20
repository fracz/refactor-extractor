commit fa77d03fea210ef67a811b603bb6e55605752654
Author: Timothy Wall <twalljava@java.net>
Date:   Thu Jan 24 17:33:12 2008 +0000

    Provide explicit dispose on NativeLibrary
    Cache NativeMappedConverter instances for improved performance
    Preliminary support for wince (improve backwards compatibility for older VMs)
    Provide 'synch after call' interface for arguments that need to perform some sort of synchronization after a native call

    git-svn-id: https://svn.java.net/svn/jna~svn/trunk@464 2f8a963e-d2e4-e7d0-97bf-ccb7fcea9d80