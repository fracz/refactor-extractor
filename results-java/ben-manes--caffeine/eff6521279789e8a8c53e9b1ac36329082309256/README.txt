commit eff6521279789e8a8c53e9b1ac36329082309256
Author: Ben Manes <ben.manes@gmail.com>
Date:   Wed Jul 15 20:01:38 2015 -0700

    Restore optimizations (removed while debugging)

    These optimizations were removed when debugging, but turned out to be
    unrelated. The memory barriers produced impact performance and do not
    provide visibility improvements, since these races are tolerated and
    acceptable on our target platform.