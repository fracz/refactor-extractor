commit d1ad9ab3121cb54387c5d2b71b48708dcaed5c43
Author: Selim Cinek <cinek@google.com>
Date:   Tue Mar 1 17:52:20 2016 -0800

    Adjusted the behavior when expanding the notification panel

    The topmost notification is now not collapsed anymore but slides in
    expanded from the top. Therefore it's layout doesn't need to animate
    anymore. This also cleans up a lot of code in the algorithm which was
    depending on this resizing which isn't needed anymore.
    In order to have that transition working perfectly, the padding when
    collapsing a card is now the same as the slowdown length.
    Also improved the logic when notification contents are allowed to
    animate and when not.
    Also fixed a bug where the top notification was cut off when QS was
    expanded.

    Bug: 27402534
    Bug: 18434284
    Change-Id: I31afa199d38d94e74fd45500c2236ffdb51d989d