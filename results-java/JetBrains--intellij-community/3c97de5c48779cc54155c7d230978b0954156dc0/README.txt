commit 3c97de5c48779cc54155c7d230978b0954156dc0
Author: Eugene Zhuravlev <jeka@jetbrains.com>
Date:   Wed Mar 18 14:30:45 2009 +0300

    1. Intrduced LazyRangeMarkerFactory service
    2. OpenFileDescriptor now uses lazy range markers to avoid unnecesary document loading and improve memory consumption