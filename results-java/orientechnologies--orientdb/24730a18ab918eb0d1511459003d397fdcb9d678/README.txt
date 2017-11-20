commit 24730a18ab918eb0d1511459003d397fdcb9d678
Author: l.garulli@gmail.com <l.garulli@gmail.com@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Sat Aug 6 11:26:24 2011 +0000

    Index:
    - fixed a bug on index remove
    - improved performance by delegating saving to the std algorithm
    - avoided lazy flush on closing