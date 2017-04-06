commit da74323141a69bd06fc19a70bc072143e9f0e986
Author: Jason Tedor <jason@tedor.me>
Date:   Mon Jun 6 22:09:12 2016 -0400

    Register thread pool settings

    This commit refactors the handling of thread pool settings so that the
    individual settings can be registered rather than registering the top
    level group. With this refactoring, individual plugins must now register
    their own settings for custom thread pools that they need, but a
    dedicated API is provided for this in the thread pool module. This
    commit also renames the prefix on the thread pool settings from
    "threadpool" to "thread_pool". This enables a hard break on the settings
    so that:
     - some of the settings can be given more sensible names (e.g., the max
       number of threads in a scaling thread pool is now named "max" instead
       of "size")
     - change the soft limit on the number of threads in the bulk and
       indexing thread pools to a hard limit
     - the settings names for custom plugins for thread pools can be
       prefixed (e.g., "xpack.watcher.thread_pool.size")
     - remove dynamic thread pool settings

    Relates #18674