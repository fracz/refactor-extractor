commit 1408eb5a58d669933c701e347fd3498ceab70f3c
Author: Selim Cinek <cinek@google.com>
Date:   Mon Jun 2 14:45:38 2014 +0200

    Changed the overscroll and expanding behaviour.

    Only the first selected element will be expanded, no subsequent children.
    Afterwards, overscrolling is performed.
    This improves overscroll consistency a lot and people don't accidentally
    expand unwanted notifications, just the one they wanted to.
    If the users primary intent is overscrolling (i.e if he drags on a card
    which is already expanded), then we allow him to go to the quick settings.

    Bug: 14487435
    Bug: 15181651
    Change-Id: I978cc4e06ae85c2ca69e15a149cb85ac54b2ef35