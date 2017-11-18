commit b904863476991d8540d37d542c0a49b78deab680
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
    Change-Id: I74e7c493468ee0088eb615c9a7fe30b4d7cf27de