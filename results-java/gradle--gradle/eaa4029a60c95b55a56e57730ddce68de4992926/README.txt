commit eaa4029a60c95b55a56e57730ddce68de4992926
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Mar 1 15:35:52 2013 +0100

    Capped the default number of parallel threads to 4. According to my tests, running on >4 worker threads (even on 8-core box) does not give performance benefits or even makes things slower. The user can still choose a specific number of parallel threads via command line. We should update the default / cap once we finish some performance improvements (e.g. multi-process locking, task history cache, and friends).