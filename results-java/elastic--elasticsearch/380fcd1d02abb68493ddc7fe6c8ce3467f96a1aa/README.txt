commit 380fcd1d02abb68493ddc7fe6c8ce3467f96a1aa
Author: Simon Willnauer <simonw@apache.org>
Date:   Fri Jan 30 09:51:05 2015 +0100

    Reset MergePolicProvider settings only if the value actually changed

    Due to some unreleased refactorings we lost the persitence of
    a perviously set values in MergePolicyProvider. This commit adds this
    back and adds a simple unittest.

    Closes #8890