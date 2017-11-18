commit 8303bd2b17eb33dcecf2ef63d1aee87f7427f812
Author: Muyuan Li <muyuanli@google.com>
Date:   Wed Mar 23 20:08:26 2016 -0700

    sysui: refactor for extension

    Refactored ZenModePanel:
    1. Button creation is split from onFinishInflate.
    2. Added a method to query current Zen condition
    3. Made mZenButtonsCallback protected.

    Change-Id: I959fa2f7770ba1888af01eababe7c4512981332d
    (cherry picked from commit 79df200108c5240c0d2a041d7d841f8d9d143ec0)