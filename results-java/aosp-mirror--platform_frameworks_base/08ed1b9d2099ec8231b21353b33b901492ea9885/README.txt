commit 08ed1b9d2099ec8231b21353b33b901492ea9885
Author: Eric Laurent <elaurent@google.com>
Date:   Mon Nov 5 14:54:12 2012 -0800

    AudioService: improve low end dock audio control

    Low end docks have a menu to enable use of audio
    for media: automatically enabling/disabling use of audio
    when the dock is connected/disconnected is useless
    and can be the source of audio glitches.

    Bug 7463620.

    Change-Id: I3b7e7ebe660bb3f0e4367d2a3ed63ee76f78fe58