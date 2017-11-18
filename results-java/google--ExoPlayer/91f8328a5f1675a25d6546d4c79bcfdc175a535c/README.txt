commit 91f8328a5f1675a25d6546d4c79bcfdc175a535c
Author: olly <olly@google.com>
Date:   Thu Jul 28 08:48:37 2016 +0100

    UI component improvements

    - Make sure no events are posted on PlaybackControlView
      if it's not attached to a window. This can cause leaks.
      The target hide time is recorded if necessary and
      processed when the view is re-attached.
    - Deduplicated PlaybackControlView.VisibilityListener
      invocations.
    - Fixed timeouts to be more intuitive (I think).
    - Fixed initial visibility of PlaybackControlView when
      used as part of SimpleExoPlayerView.
    - Made some more attributes configurable from layout xml.

    Issue: #1908
    Issue: #1919
    Issue: #1923

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=135679988