commit 6bf7dcd0ce4319b935b227110de3aadaf6215691
Author: l.garulli@gmail.com <l.garulli@gmail.com@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Mon Apr 25 01:04:16 2011 +0000

    Huge refactoring about the management of the connections:
    - clients can handle multiple channels for the same server/db
    - used thread local to keep sessionId at client side