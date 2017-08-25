commit 29983c16711b7eb6828afab8ca87a09eec9f6fe3
Author: alecpl <alec@alec.pl>
Date:   Sat Oct 9 16:46:53 2010 +0000

    - Add caching support in id2uid and uid2id functions (#1487019), Fix get_message_cache_index
      to use internal cache when only sort order changes. Both changes improves performance of 'show' action
    - code cleanup/function description fixes