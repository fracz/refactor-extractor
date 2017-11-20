commit 62e3d4793fda599654fba7ba2c0c415031d922de
Author: Manu Goyal <manu.goyal2013@gmail.com>
Date:   Fri Sep 4 17:16:50 2015 -0700

    Made TachyonFileSystem methods throw better exceptions

    We were turning everything into an IOException, which can make it hard
    for callers to determine what went wrong in an operation. So now the
    TachyonFileSystem and related classes don't wrap exceptions in an
    IOException.

    There are a lot of miscellaneous refactors for deprecated code and tests
    to get this to compile.