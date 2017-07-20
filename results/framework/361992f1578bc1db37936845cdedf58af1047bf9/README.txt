commit 361992f1578bc1db37936845cdedf58af1047bf9
Author: Andrew <browner12@gmail.com>
Date:   Thu Jul 6 16:13:50 2017 -0500

    remove temporary variable (#19935)

    * remove temporary variable

    remove the temporary `$aliases` variable and refactor the array into the `foreach` loop.

    is this crazy? unecessary?

    I think technically it should be **less memory usage** (much much less), but it's still something

    and I don't think we lose readability on it.

    curious what people think.

    * styleCI fix