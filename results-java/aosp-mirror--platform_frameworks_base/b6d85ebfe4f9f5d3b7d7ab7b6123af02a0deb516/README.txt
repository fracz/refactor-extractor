commit b6d85ebfe4f9f5d3b7d7ab7b6123af02a0deb516
Author: Selim Cinek <cinek@google.com>
Date:   Fri Mar 28 20:21:01 2014 +0100

    Enabled the new notification shade and improved expanding logic

    Made the NotificationStackScroller now the default and only shade.
    When the notification shade is expanded, the NotificationStackScroller
    now also expands revealing the notifications.

    Change-Id: If989ed848f684b3ac4e687d9642289db4599553b