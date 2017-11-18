commit 56fd70cd8ce992cf7f673908540607365b643743
Author: Jorim Jaggi <jjaggi@google.com>
Date:   Thu Oct 27 19:57:08 2016 -0700

    The big Keyguard transition refactor (7/n)

    There was some window animation jank because we ran a layout during
    the animation, as the SysUI animations are slightly faster, so we
    collapsed the window already during the animation which caused
    a layout which caused window animation jank.

    Bug: 32057734
    Change-Id: I296f961be8cfc39b08859b7d3d41f1e81b2eaaa3