commit 16b8251c89743dab939ccfc79fb6f64d0df6aaae
Author: Muyuan Li <muyuanli@google.com>
Date:   Sat Mar 19 15:50:45 2016 -0700

    Refactor enableAccessibility to AccessibilityManagerService

    enableAccessibility is refactored from EnableAccessibilityController to
    AccessibilityManagerService. Also added are 2 methods disableAccessibility,
    and isAccessibilityEnabled.

    Bug: 27645255
    Change-Id: I32d75ed6617b8bcf82bbee0dd5ee776f430fb386
    (cherry picked from commit 84da556422d50e43eb674061cc454f331104d493)