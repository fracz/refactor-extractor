commit 492bbe3f7b42860dafe5435203ea30278bf76b6c
Author: Ben Murdoch <benm@google.com>
Date:   Thu Sep 1 03:27:26 2011 +0100

    Preload libchromium_net in addition to libwebcore.

    To improve WebView app startup time, preload libchromium_net which
    was recently split from libwebcore into its own shared library.

    Bug: 5112647
    Change-Id: I4417d5a4f8c7783e8fa7b8eaddf89aaeb3693fac