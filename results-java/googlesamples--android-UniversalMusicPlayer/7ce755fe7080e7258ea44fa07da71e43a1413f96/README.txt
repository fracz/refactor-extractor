commit 7ce755fe7080e7258ea44fa07da71e43a1413f96
Author: Nagesh Susarla <nageshs@google.com>
Date:   Wed Jan 7 17:42:03 2015 -0800

    1. Playback controls not shown when remote playback is active
    Ensure that when remote playback is active, the playback controls are shown across the app.
    Also refactored the fragment to handle the metadata/playbackstate changes as soon as the
    mediaBrowser is connected. (The issue of reusing the fragment earlier was that an empty
    control would be shown if the MediaController was not set by the time onStart() was called.)

    2. Add metadata handling from remote cast media information. This involves passing a mediaId
    as customData while loading the media and using that information to update the metadata.

    3. Fix position handling across cast and local playback.

    Bug: 18934482
    Bug: 18932274
    Bug: 18820962
    Bug: 18817883
    Bug: 18936489
    Change-Id: Ib9aa278e9fa63529b7a058f6eea50a6995164318