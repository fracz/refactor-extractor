commit f88d8082a86bee00c604cbbcfb5261f5573936fe
Author: John Spurlock <jspurlock@google.com>
Date:   Wed Mar 25 18:09:51 2015 -0400

    Introduce new volume dialog.

     - New VolumeDialog (presentation) + VolumeDialogController (state)
       to implement a volume dialog that keeps track of multiple audio
       streams, including all remote streams.
     - The dialog starts out with a single stream, with more detail available
       behind an expand chevron.
     - Existing zen options reorganized under a master switch bar
       named "Block interruptions", with "None" renamed to "No interruptions"
       and "Priority" renamed to "Priority only".
     - Combined "Block interruptions" icon replaces the now-obsolete star/no-smoking
       icons in the status bar.
     - New icons for all sliders.
     - All sliders present a continuous facade, mapped to discrete integer units
       under the hood.
     - All interesting volume events and state changes piped through one central
       helper for future routing.
     - VolumePanel is obsolete, still accessible via a sysprop if needed.
       Complete removal / garbage collection deferred until all needed
       functionality is ported over.

    Bug: 19260237
    Change-Id: I6689de3e4d14ae666d3e8da302cc9da2d4d77b9b