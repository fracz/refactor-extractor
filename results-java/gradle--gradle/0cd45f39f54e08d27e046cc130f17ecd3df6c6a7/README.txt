commit 0cd45f39f54e08d27e046cc130f17ecd3df6c6a7
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Dec 6 17:33:31 2011 +0100

    GRADLE-1933 Tooling api thread safety.

    Some refactoring around embedded mode for the DefaultConnection. I'm not 100% sure if it is necessary to keep some state around for the DefaultConnection for the embedded mode so I left this as it worked before. We are not concerned about concurrency for the embedded tooling api at the moment, anyway.