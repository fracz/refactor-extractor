commit 8b76fe5a1aaf96f849ea5543ca3858014d25f5a4
Author: Philipp Wollermann <philwo@google.com>
Date:   Mon Sep 14 11:50:01 2015 +0000

    workers: A multitude of bug fixes and improved logging.

    I know this should have been split up, but I was a bit on fire today and did it all in one go ^^;

    Fixes spurious "Stream closed: Stream closed" errors, by noticing dead workers and retrying with a fresh one. (Configurable with the --worker_max_retries flag.)

    Fixes an "IllegalArgumentException" when a non-worker compatible Spawn is given to the strategy. We fall back to StandaloneSpawnStrategy now.

    Redirect the stderr of worker processes to separate log files in a common sub-directory and print a message that tells you about the location on worker start-up for easier debugging. The log can be found in <output_base>/worker-logs/*.log.

    Adds the mnemonic of the Spawn to log messages and the log filename.

    Adds verbose messages on worker start-up and shutdown. (Enable it with --worker_verbose!)

    Shuts down the worker pool after a build finished by default, until we sort out one last remaining correctness issue. This also conserves resources, though makes incremental builds a bit slower. Want the maximum performance anyway? Try --experimental_workers_keep_running.

    Adds stack traces to errors that are caused by buggy workers to aid development.

    Fixes weird dupli..tripli..quadruple error messages ("Compiling failed: Stream closed: Stream closed: Stream closed: Stream closed.").

    --
    MOS_MIGRATED_REVID=102983853