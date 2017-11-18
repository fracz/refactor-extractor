commit 18183d8c26d40f0294cfc0fb1c11e39700f7049a
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sun Sep 8 14:51:09 2013 +0200

    On the way to improve the in-memory task history cache. Now we're able to hook in some in-memory decoration and we have access to methods on UnitOfWorkParticipant (for possible expiration of the in memory data).