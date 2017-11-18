commit acc9cea8f606b5093df074a4213e094844cc3ecd
Author: Jason Tedor <jason@tedor.me>
Date:   Mon Jun 6 23:26:22 2016 -0400

    Fix compilation issue in RefreshListenersTests

    This commit fixes a compilation issue in RefreshListenersTests that
    arose from code being integrated into master, and then a large pull
    request refactoring the handling of thread pools was later merged into
    master.