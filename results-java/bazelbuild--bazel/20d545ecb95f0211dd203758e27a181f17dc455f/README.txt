commit 20d545ecb95f0211dd203758e27a181f17dc455f
Author: Philipp Wollermann <philwo@google.com>
Date:   Tue May 9 08:30:23 2017 -0400

    sandbox: Switch Darwin sandbox to the SymlinkedExecRoot strategy.

    Hardlinks are problematic due to not working across filesystem
    boundaries and causing Bazel to do lots of I/O because it has to create
    a hardlink and a symlink for each input file.

    This improves performance of Bazel building itself by 10% on my system.

    Change-Id: I8acb77053de875160a046e38624735ed18375bed
    PiperOrigin-RevId: 155493583