commit 07024a4e4b6c0303f893c9d5020a66652a037f7a
Author: Osvaldo Doederlein <opinali@google.com>
Date:   Sat Jul 19 18:16:50 2014 -0400

    Fixes and improvements to IntObjectHashMap. Related to [#2659]

    - Rewrite with linear probing, no state array, compaction at cleanup
    - Optimize keys() and values() to not use reflection
    - Optimize hashCode() and equals() for efficient iteration
    - Fixed equals() to not return true for equals(null)
    - Optimize iterator to not allocate new Entry at each next()
    - Added toString()
    - Added some new unit tests