commit 64183d518813cc65efcaeaf0d4c28de092b32868
Author: Makoto Onuki <omakoto@google.com>
Date:   Mon Aug 8 14:11:34 2016 -0700

    ShortcutManager improve app udpate check

    - Don't use lastUpdateTime for system apps since it's not reliable.

    - Scan downgraded apps too.

    Bug 30708050
    Bug 30734178
    Change-Id: I98253f4c635466197548385275ab08c5e3a1a10b