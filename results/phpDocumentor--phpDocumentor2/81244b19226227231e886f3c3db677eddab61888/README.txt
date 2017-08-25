commit 81244b19226227231e886f3c3db677eddab61888
Author: Mike van Riel <me@mikevanriel.com>
Date:   Tue Jul 8 18:09:33 2014 +0200

    #1330: Fix crash when assembling package tags

    In a recent refactoring we removed all notion of string based tags
    but put them in TagDescriptors. Apparently I missed a bit of handling
    of the package tags and this caused fatal errors later due to typehints.