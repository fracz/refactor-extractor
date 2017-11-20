commit fe83409b62ec5387de8b91a397812b6e89495eba
Author: Ben Manes <ben.manes@gmail.com>
Date:   Wed Apr 29 19:05:26 2015 -0700

    ConcurrentLinkedLazyQueue as a faster alternative to CLQ

    An optimistic queue where the next field is lazily updated when appending and
    fixed on removal / traversal. A combining arena is used to improve performance
    of multiple producers appending to the queue concurrently. This implementation
    provides a O(1) bulk removal of all elements.