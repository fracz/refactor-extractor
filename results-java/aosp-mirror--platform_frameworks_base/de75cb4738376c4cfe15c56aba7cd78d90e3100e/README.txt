commit de75cb4738376c4cfe15c56aba7cd78d90e3100e
Author: Dianne Hackborn <hackbod@google.com>
Date:   Wed Mar 2 17:11:21 2011 -0800

    Fix issue #3400119: API to specify a black background behind a window transition

    There is now an API, which is used for task switching.

    Also improved how we handle rotation animation when we can't take a
    screen shot, to cleanly revert to the old freeze behavior.  This removes
    the need to special case the emulator.

    Change-Id: I7227432a2309370437ec6ac78db02c6f1e7eedd5