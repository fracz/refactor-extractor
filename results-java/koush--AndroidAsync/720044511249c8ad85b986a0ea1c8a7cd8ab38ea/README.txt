commit 720044511249c8ad85b986a0ea1c8a7cd8ab38ea
Author: Zbigniew Szymański <zbigniew.szymanski@wearezeta.com>
Date:   Sat Mar 29 21:12:29 2014 +0100

    PushParser refactoring
    - simplified implementation, removed couple unused methods
    - type safe TapCallback - no need for reflection, no problems with proguard