commit 82da8d9d071c375440437a6913c21e10210d0ca4
Author: Schlaefer <openmail+github@siezi.com>
Date:   Thu Sep 5 12:22:28 2013 +0200

    performance improvements in CakeTime::timeAgoInWords Edit

    - rearranges code to move return statements before otherwise dead code
    - only do translation if translated string is actually used