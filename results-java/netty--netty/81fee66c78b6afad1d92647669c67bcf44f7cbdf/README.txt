commit 81fee66c78b6afad1d92647669c67bcf44f7cbdf
Author: Norman Maurer <norman_maurer@apple.com>
Date:   Tue May 19 15:03:29 2015 +0200

    Let PoolThreadCache work even if allocation and deallocation Thread are different

    Motivation:

    PoolThreadCache did only cache allocations if the allocation and deallocation Thread were the same. This is not optimal as often people write from differen thread then the actual EventLoop thread.

    Modification:

    - Add MpscArrayQueue which was forked from jctools and lightly modified.
    - Use MpscArrayQueue for caches and always add buffer back to the cache that belongs to the allocation thread.

    Result:

    ThreadPoolCache is now also usable and so gives performance improvements when allocation and deallocation thread are different.

    Performance when using same thread for allocation and deallocation is noticable worse then before.