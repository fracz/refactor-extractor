commit 73294b6cf79910dc688e5b62d673082a3dec773b
Author: Jorim Jaggi <jjaggi@google.com>
Date:   Wed Oct 26 18:02:36 2016 -0700

    The big keyguard transition refactor (4/n)

    Nuke KeyguardScrim

    Test: Kill SystemUI while lockscreen is showing, make sure nothing
    is visible when being killed.

    Bug: 32057734
    Change-Id: I9f8d1e5a0e0f968460d8170627a849623c6a7245