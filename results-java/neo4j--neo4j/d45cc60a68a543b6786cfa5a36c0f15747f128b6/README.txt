commit d45cc60a68a543b6786cfa5a36c0f15747f128b6
Author: Jacob Hansson <jakewins@gmail.com>
Date:   Tue May 26 21:22:10 2015 -0500

    Introduce session worker threads.

    This decouples request execution from IO threads, resolving several pesky NDP
    deadlock issues, as well as opening up doors for performance improvements where
    IO threads can focus on request deserialization, and worker threads focus on
    query execution.

    This design uses one thread per session started, under the assumption that the
    average database will deal with <= 10 000 live sessions per database instance.

    This should resolve deadlock issues seen on single-cpu systems.