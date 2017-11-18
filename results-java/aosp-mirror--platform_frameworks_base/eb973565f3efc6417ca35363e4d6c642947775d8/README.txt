commit eb973565f3efc6417ca35363e4d6c642947775d8
Author: Selim Cinek <cinek@google.com>
Date:   Fri May 2 17:07:49 2014 +0200

    Refactored the notification animations, improved stack scroller

    Animations are now only triggered when absolutely needed.
    In addition, the notifications are now not clipped anymore when starting
    a drag on them and the notification below the dragged one is fadded in if
    necessary.

    Change-Id: I80e8b3ea8fb48505edfb3cace6176dfa00c5a659