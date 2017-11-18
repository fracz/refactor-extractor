commit 20830421fe223bf2a8a69a67a6d26b0b5beb5baa
Author: Jim Miller <jaggies@google.com>
Date:   Tue May 15 20:46:03 2012 -0700

    Fix 6398209: Lots of improvements to gesture search from navbar

    - Added ability to postpone animations until after window is shown
    to ensure the animation is visible on slower devices.
    - Fixed layout bug that prevented targets from being located on
    outer ring.
    - Fixed bug where some motion events were being ignored when handle
    wasn't captured.
    - Reduced temp object generation in several methods.
    - Added containers to start all animations together and to facilitate
    delayed start.
    - Increased radius of outer ring on phones to closer match mocks.
    - Decreased sensitivity of swipe up gesture on navbar so it's harder to false.

    Change-Id: I084f840115aef6496a1f87202c4e42d9962c8c3e