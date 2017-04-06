commit 736c8fbbae57a209f4ba570e38cc3bd0745a9569
Author: Jeff Cross <middlefloor@gmail.com>
Date:   Tue Nov 26 11:36:36 2013 -0800

    refactor($location): move file://+win path fix to $location

    The urlResolve method was fixed to automatically remove the
    volume label from path names to fix issues with the file
    protocol on windows where $location.path() was returning
    paths where the first segment would be the volume name,
    such as "/C:/mypath". See #4942 and #4928

    However, the solution was specific to the $location non-
    HTML5 mode, and was implemented at a lower level of
    abstraction than it should have been. This refactor moves
    the fix to inside of the LocationHashBangUrl $$parse method.

    Closes #5041