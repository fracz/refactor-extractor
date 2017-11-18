commit bcb9f8282df7b05a66329e5115511c67f1651d2d
Author: Oliver Woodman <olly@google.com>
Date:   Mon Oct 12 12:32:10 2015 +0100

    Enable SmoothFrameTimeHelper by default.

    Context:
    - Currently, playback is significantly more juddery with it disabled,
      particularly on AndroidTV.
    - We should be able to do the "best" job of this internally, so injection
      doesn't buy anything useful. If someone has a better implementation for
      adjusting the frame release, they should improve the core library.