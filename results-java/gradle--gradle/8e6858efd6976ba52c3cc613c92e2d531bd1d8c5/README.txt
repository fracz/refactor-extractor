commit 8e6858efd6976ba52c3cc613c92e2d531bd1d8c5
Author: Cedric Champeau <cedric@gradle.com>
Date:   Tue Aug 30 13:41:51 2016 +0200

    Make it possible to generate flame graphs during performance tests

    This commit improves the integration of Honest Profiler by making it possible to post-process the log files and generate a flame graph.
    For this to be possible, the `org.gradle.performance.honestprofiler` system property must be set to a directory where flame graphs will
    be exported.

    It is expected to find 2 environment variables:

    - `HP_HOME_DIR` must point to a valid Honest Profiler installation (https://github.com/RichardWarburton/honest-profiler)
    - `FG_HOME_DIR` must point to a valid FlameGraph installation (https://github.com/brendangregg/FlameGraph)

    In case those are not set, the collector will try to find them in `~/tools/honest-profiler` and `~/tools/FlameGraph` respectively.

    The graphs are *not* integrated into the performance reports yet.