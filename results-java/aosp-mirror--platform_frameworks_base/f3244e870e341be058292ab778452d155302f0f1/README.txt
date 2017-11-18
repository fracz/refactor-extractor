commit f3244e870e341be058292ab778452d155302f0f1
Author: Aga Wronska <agawronska@google.com>
Date:   Wed Mar 16 13:44:27 2016 -0700

    Revert "Precompute cursor indexes in DocumentsUI and improve perf by 2.7%."
    It ia causing tests failures and exception at the app start.

    This reverts commit d35a974b76cb0b3387aff7780d101e3f7de2ebd3.

    Change-Id: I2577f723a7e25d4dcc12050791c65a900ac41f7d