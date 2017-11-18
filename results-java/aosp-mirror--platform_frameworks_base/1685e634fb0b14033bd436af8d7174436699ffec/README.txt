commit 1685e634fb0b14033bd436af8d7174436699ffec
Author: Selim Cinek <cinek@google.com>
Date:   Tue Apr 8 02:27:49 2014 +0200

    Further improved NotificationStackScroller

    The top card is now collapsed during the pulldown of
    the notification shade and expanded during the transition.
    The scrollstate is also reset once the shade is closed.

    Change-Id: Ibf17eef1f338d674c545e5bf55261e60db62b2ce