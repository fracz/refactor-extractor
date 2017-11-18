commit 9c2c77f8bd8288dcafc9f8111396d46b896d1933
Author: Adam Powell <adamp@google.com>
Date:   Thu Nov 1 14:52:17 2012 -0700

    Keyguard - fix overzealous sliding security view

    Bug 7453429

    Change the detection of sliding the security view to require crossing
    the border between sections in the correct direction. This also
    improves the feel of re-opening the slider.

    Change-Id: I57797f926e017ea2cf41f7c48e0fe77ac0f78460