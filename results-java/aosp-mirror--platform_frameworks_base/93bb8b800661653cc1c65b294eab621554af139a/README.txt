commit 93bb8b800661653cc1c65b294eab621554af139a
Author: Chris Wren <cwren@android.com>
Date:   Tue Mar 29 14:35:05 2016 -0400

    actually cancel sounds when we get a quiet update

    Also add some tests to the notification manager service
    to make the refactor more palatable.

    Bug: 27742532
    Change-Id: I745915299aa2b92ab077e0c801d47864cb562325