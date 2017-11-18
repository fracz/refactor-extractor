commit 4508218850faedea95371188da587b6734f5f3da
Author: sergeyv <sergeyv@google.com>
Date:   Thu Sep 29 18:25:40 2016 -0700

    Make bitmap backed by native memory instead of java byte array
    Test: refactoring CL. Existing unit tests still pass.
    bug:27762775

    Change-Id: Ic4e914b3a941c3d545f8ce9e320e638973df0e91