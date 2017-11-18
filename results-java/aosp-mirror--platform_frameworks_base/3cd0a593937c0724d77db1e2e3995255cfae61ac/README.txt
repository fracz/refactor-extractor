commit 3cd0a593937c0724d77db1e2e3995255cfae61ac
Author: Michael Jurka <mikejurka@google.com>
Date:   Tue Aug 16 12:40:30 2011 -0700

    Improve Recent Apps scrolling performance

    - 20fps improvement using software rendering
    - 10fps improvement using hardware rendering
    - in sw mode, rendering recents background in the recent items themselves and using a bitmap cache to draw individual items (gives perf gains for sw mode)
    - in sw and hw mode, no longer doing a fade on the recents scroll view (gives perf gains for hw mode) - instead we draw a black gradient where we would normally fade
    - fading recents & notifications immediately when swiped
    - removing unused code

    Change-Id: I908e2a25b89c9dfbf9b8c8f3810fa43064261b33