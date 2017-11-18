commit bc303b730ad34d421a14598f05cba26ed34eacb1
Author: Oliver Woodman <olly@google.com>
Date:   Tue Nov 18 19:03:04 2014 +0000

    Factor out AudioTrack from MediaCodecAudioTrackRenderer.

    AudioTrack contains the portions of MediaCodecAudioTrackRenderer that handle the
    platform AudioTrack instance, including synchronization (playback position
    smoothing), non-blocking writes and releasing.

    This refactoring should not affect the behavior of audio playback, and is in
    preparation for adding an Ac3PassthroughAudioTrackRenderer that will use the
    AudioTrack.