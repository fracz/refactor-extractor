commit 31094df5c6e3cb3a4a4faacb091e35eea1f6a5de
Author: Selim Cinek <cinek@google.com>
Date:   Thu Aug 14 19:28:15 2014 +0200

    Fixed several bugs with the notification shade

    Cleaned up the code around mMaxPanelHeight of the
    PanelView which could lead to flickering during
    peeking.
    Changed the panel opening logic to account for lag
    when we need to wait for a layout, which could lead
    to inconsistent animations.
    Fixed a bug where holes could appear in the shade
    when notifications were updating.
    This also improved the general updating behaviour
    which is now done in a nicer animation.

    Bug: 15942322
    Bug: 15861506
    Bug: 15168335
    Change-Id: Ifd7ce51bea6b5e39c9b76fd0d766a7d2c42bf7a4