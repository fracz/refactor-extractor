commit b9c9d260f21b321527c4622a123af9767630d94d
Author: Eric Laurent <elaurent@google.com>
Date:   Wed May 6 08:13:20 2009 -0700

    fix issue 1713090: After a Bluetooth call, MusicPlayer starts playing on speaker rather than wired external audio.

    Temporary fix until audio routing is refactored in Eclair release:
    - centralized and synchronized all audio routing control in AudioService.setRouting()
    - deprecated AudioManager.setRouting() and AudioManager.getRouting() methods