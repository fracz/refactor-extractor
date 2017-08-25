commit 86ca6a10cd2bce5f930d97b4a12032043673d58a
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Tue Feb 5 17:05:46 2013 +0100

    Replace XML references to ProjectDescriptor in Transformer

    During this series of changes we have disabled the behaviours and started
    refactoring the transformer objects to use the object model instead of the
    xml output as it was before.

    Some boyscouting was also executed in the sense of code cleanups