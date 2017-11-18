commit 94931bd87e27e766167cf005788b148af49f6ac2
Author: John Reck <jreck@google.com>
Date:   Wed Sep 28 16:12:42 2016 -0700

    Matrix JNI update

    Switches to @FastNative & @CriticalNative
    Switches to NativeAllocationRegistry
    Updated formatting
    Changes native_* to n* naming for native methods

    Test: refactor CL, no behavior change; device still boots

    Change-Id: Ic3b115b7aef26811bf8fef3777c131778608da30