commit 775b0ad3edd047a32ef8a144a4d5a4b28c30eb7c
Author: Rene Groeschke <rene@breskeby.com>
Date:   Sun Mar 11 14:58:20 2012 +0100

    GRADLE-2149 some refactorings
    - Moved libc loading logic to FilerPermissionHandlerfactory.
    - Rename NativeFilerPermissionHandler to ComposableFilePermissionHandler.
    - Refactor filepermissionhandler related tests.