commit 00ff604aa74bc1baa80ade1deb8ae07d6b4c45db
Author: Andrew Sapperstein <asapperstein@google.com>
Date:   Thu Jun 23 10:09:27 2016 -0700

    Fix crash when tapping on cell icon in QS.

    During a refactoring, accidentally tried to use a color int
    as a color resource int.

    Change-Id: I4a96ffea9a997ad3cf19029b1101a283b7e88459
    FIXES: 29575154