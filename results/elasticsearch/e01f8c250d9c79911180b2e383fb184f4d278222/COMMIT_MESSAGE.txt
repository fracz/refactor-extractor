commit e01f8c250d9c79911180b2e383fb184f4d278222
Author: Adrien Grand <jpountz@gmail.com>
Date:   Tue Jan 7 14:42:44 2014 +0100

    Change the default recycler type.

    Recycling is not thread-local anymore by default but instead there are several
    pools of objects to recycle that threads may use depending on their id.
    Each pool is protected by its own lock so up to ${number of pools} threads may
    recycler objects concurrently.

    Recyclers have also been refactored for better composability, for example there
    is a soft recycler that creates a recycler that wraps data around a
    SoftReference and a thread-local recycler that can take any factory or recyclers
    and instantiates a dedicated instance per thread.

    RecyclerBenchmark has been added to try to figure out the overhead of object
    recycling depending on the recycler type and the number of threads trying to
    recycle objects concurrently.

    Close #4647