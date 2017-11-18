commit 5f5ec7b8d58bf90f16ea0b64e5e7a6da86b98aff
Author: Oliver Woodman <olly@google.com>
Date:   Wed Mar 29 18:06:46 2017 +0100

    Avoid unnecessary object allocation in CryptoInfo.set

    This is causing a large number of unnecessary small object allocations
    during encrypted playbacks, which presumably all need to be GC'd.

    I wasn't sure whether the zero pattern should be static; that might
    be objectionable in the platform (unsure?), since it would live
    forever from the point of the class being classloaded. It doesn't
    make much/any difference in practice in any case.

    Test: Safe refactoring CL.
    Change-Id: I9ee5fe284b0f854d672d83b97fc51116b0416f91