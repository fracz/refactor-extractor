commit 4929a5d3b434a6915d64e449b234b40b8eff9993
Author: Rubin Xu <rubinxu@google.com>
Date:   Mon Jan 23 23:55:28 2017 +0000

    Fix NPE in LockPatternUtils

    mDevicePolicyManager field can be null if getDevicePolicyManager()
    is never called. A previous refactor removed an apparent unused call
    to getDevicePolicyManager(), leading to NPE when mDevicePolicyManager
    is accessed. We should always use getDevicePolicyManager() to access
    DevicePolicyManager instance.

    Test: manual
    Bug: 34612758
    Change-Id: Ic964b4ee4e3c56301295b8f0629bd005c732c5c4