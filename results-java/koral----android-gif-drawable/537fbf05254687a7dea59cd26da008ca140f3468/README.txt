commit 537fbf05254687a7dea59cd26da008ca140f3468
Author: koral-- <koral--@users.noreply.github.com>
Date:   Mon Jan 20 00:38:28 2014 +0100

    color index computing fixes, including
    https://code.google.com/p/skia/source/detail?r=12786
    throwing exceptions for errors during initializing moved to native code
    input stream closing moved to native code
    screen size checking changed to handle overflows
    minor performance improvements in native code