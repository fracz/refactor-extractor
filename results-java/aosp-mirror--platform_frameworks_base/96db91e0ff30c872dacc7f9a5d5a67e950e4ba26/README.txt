commit 96db91e0ff30c872dacc7f9a5d5a67e950e4ba26
Author: Narayan Kamath <narayan@google.com>
Date:   Tue May 20 13:55:09 2014 +0100

    Improve ABI handling for shared user ids.

    The key improvement is that we need to keep track of
    the package that's currently being scanned (this includes
    new installs and upgrades of existing packages) and treat
    it specially. If we didn't do that, In the case of upgrades
    we would perform the shared UID calculation based on the ABI
    of the old package, and not the current package.

    This change also allows us to perform the CPU ABI calculation
    before dexopt, which saves us from having to do it twice and
    fixes a bug where we were using the wrong package path to
    dexopt a package.

    This also has the side effect of fixing 15081286.

    bug: 15081286

    (cherry-picked from commit b851c89d2252cf3d1dc504558ce1553527885316)

    Change-Id: I20f8ad36941fc3df29007f0e83ce82f38f3585c8