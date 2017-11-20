commit 44ff843e9ee6b3f1acab6d5b01da3ed1fbf2a903
Author: l.garulli@gmail.com <l.garulli@gmail.com@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Tue Nov 15 17:34:29 2011 +0000

    Fixed issue 538:
    - improved performance on transaction,
    - improved reliability, since now a classic file is used, not a mmap
    - temporary records are now inside the tx log and this reduce fragmentation as well
    - this is a brick to create the server log for replication