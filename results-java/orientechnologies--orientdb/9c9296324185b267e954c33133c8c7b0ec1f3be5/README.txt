commit 9c9296324185b267e954c33133c8c7b0ec1f3be5
Author: l.garulli@gmail.com <l.garulli@gmail.com@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Fri May 27 14:56:23 2011 +0000

    Working to the issue 327. Not closed yet but improved performance without converting the field anymore if not necessary.

    This has been optimized at deeper level for each field loading by SQL Engine. Now uses always should ODocument.rawField() instead of ODocument.field().