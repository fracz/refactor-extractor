commit 317effcd5010400f62fdd9d626f7023869798f40
Author: Selim Cinek <cinek@google.com>
Date:   Thu Jun 29 17:21:31 2017 +0200

    Improved Text transformations for notifications

    Notifications with different text sizes can now properly transform
    into each other using view scaling. This improves the transition
    for min priority notifications.

    Test: manual, expand min priority notification, observe smooth transformation
    Change-Id: Icc8efa8521e6a5346607f0ca330e1bc519dbb345
    Fixes: 38397448