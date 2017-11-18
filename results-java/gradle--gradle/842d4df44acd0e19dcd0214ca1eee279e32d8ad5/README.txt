commit 842d4df44acd0e19dcd0214ca1eee279e32d8ad5
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sat Sep 14 09:41:29 2013 +0200

    hasNewOwner information is propagated to the UnitOfWorkParticipants. This refactoring is needed to properly implement expiration of the in-memory task history cache.