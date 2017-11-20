commit d6d3fc9ee83923fee8925383c9db65a5c0f3e8fa
Author: Aaron Davidson <aaron@databricks.com>
Date:   Mon May 19 13:12:43 2014 -0700

    Minor improvements to resilience of Heartbeat Thread

    Improvements include:

    - The HeartbeatThread is now always daemonic, so it will not prevent the
    Master or Worker from exiting.
    - Any exceptions that propagate from the HeartbeatExecutor will be printed.
    - mIsShutdown was made volatile, and Thread.interrupt() used, to ensure
      timely shutdown of the thread. Note that either one is sufficient, but
      without either of them, the JVM does not guarantee this thread ever exits.