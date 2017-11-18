commit 2b6d792ad56795050f01abbd2d732cf717037b1c
Author: Narayan Kamath <narayan@google.com>
Date:   Mon May 18 10:18:35 2015 +0100

    Scan package to derive ABIs before optimizing.

    This requires a minor refactor to extract the ABI detection logic
    out of scanPackageDirtyLI.

    Note that there's a minor regression here : we ignore the
    cpuAbiOverride from the package settings when calculating the
    CPU ABI. This is OK (and possibly better behaviour) because this
    is only a debug only option (for adb install) AND because the instructions
    require users to specify the abi override on every adb install
    invocation. Furthermore, the behaviour when an ADB installed app
    (with an override) is auto-updated is more consistent.

    bug: 21144503

    (cherry picked from commit b904863476991d8540d37d542c0a49b78deab680)

    Change-Id: I1eb88b808fd2e90e14c32322131659220aafdb7a