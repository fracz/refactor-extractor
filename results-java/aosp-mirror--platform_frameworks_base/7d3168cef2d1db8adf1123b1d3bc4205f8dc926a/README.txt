commit 7d3168cef2d1db8adf1123b1d3bc4205f8dc926a
Author: Jean-Michel Trivi <jmtrivi@google.com>
Date:   Tue Mar 25 10:41:24 2014 -0700

    Continue refactoring of MediaFocusControl

    Move RemoteControlClient death handler and remote playback state
     inside MediaController class.
    Make MediaController data fields private. Document which accessors
     can be removed once the audio focus and media button receiver
     mechanisms are dissociated.

    Change-Id: Icd14fb0d99bf74512c8f8552d124c0353ce1f06d