commit 8320de8e29819963845d3d386d6d087844a5ae31
Author: Amith Yamasani <yamasani@google.com>
Date:   Fri Oct 5 16:10:38 2012 -0700

    Fix widget cross-talk between users due to Settings widget

    Bug: 7247911
    Bug: 7294899

    Also did some cleanup of unused code that resulted from refactoring
    the app widget service. Fixed a few more ambiguous calls that weren't
    using the correct user id.
    Added some logging and improved the dump() formatting.

    Change-Id: I27abb5c6341458e1e50a2cc9ab67e8de573ab283