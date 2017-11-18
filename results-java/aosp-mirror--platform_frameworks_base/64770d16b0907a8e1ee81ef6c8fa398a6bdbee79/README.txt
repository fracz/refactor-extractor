commit 64770d16b0907a8e1ee81ef6c8fa398a6bdbee79
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu May 23 17:51:19 2013 -0700

    Some improvements to meminfo output.

    - Rename "Swappable PSS" to "PSS Clean" which I think is what it
      means and is consistent with the other memory metrics.
    - Split at the top level the dalvik heap from other dalvik allocations,
      so when you look on the dalvik allocations line things are consistent
      with the allocator's data and it is clear what are app allocations vs.
      other data in dalvik.
    - Don't print lines that are all 0.
    - Don't print the detailed Dalvik allocation data by default; add a new
      option to have it printed.

    Here's what a typical system process dump now looks like:

    ** MEMINFO in pid 6358 [system] **
                       Pss      Pss   Shared  Private   Shared  Private     Heap     Heap     Heap
                     Total    Clean    Dirty    Dirty    Clean    Clean     Size    Alloc     Free
                    ------   ------   ------   ------   ------   ------   ------   ------   ------
      Native Heap        0        0        0        0        0        0     6964     3599     2048
      Dalvik Heap     7541        0     4344     7356        0        0    11768    11194      574
     Dalvik Other     3553        0     2792     3448        0        0
            Stack       28        0        8       28        0        0
           Cursor        4        0        0        4        0        0
           Ashmem        5        0       12        0        0        0
        Other dev     4004        0       24     4000        0        4
         .so mmap     3959      684     2500     2280     5468      684
        .apk mmap      173       68        0        0      692       68
        .dex mmap     4358     3068        0        0     9276     3068
       Other mmap       60        0        8        8      244       36
          Unknown     4387        0      508     4380        0        0
            TOTAL    28072     3820    10196    21504    15680     3860    18732    14793     2622

     Objects
                   Views:       10         ViewRootImpl:        1
             AppContexts:        8           Activities:        0
                  Assets:        3        AssetManagers:        3
           Local Binders:      176        Proxy Binders:      341
        Death Recipients:      141
         OpenSSL Sockets:        0

     SQL
             MEMORY_USED:      473
      PAGECACHE_OVERFLOW:       98          MALLOC_SIZE:       62

     DATABASES
          pgsz     dbsz   Lookaside(b)          cache  Dbname
             4       68             49         7/21/7  /data/data/com.android.providers.settings/databases/settings.db
             4       20             17         0/13/1  /data/system/locksettings.db
             4       20             21        96/14/2  /data/system/locksettings.db (1)
             4       20             21        75/13/2  /data/system/locksettings.db (2)
             4       80             29         4/17/3  /data/system/users/0/accounts.db

    Change-Id: Ifd511a7baaa8808f82f39509a5a15c71c41d1bac