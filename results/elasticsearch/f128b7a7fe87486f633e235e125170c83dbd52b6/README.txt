commit f128b7a7fe87486f633e235e125170c83dbd52b6
Author: Simon Willnauer <simonw@apache.org>
Date:   Thu Jan 26 13:58:26 2017 +0100

    Improve connection closing in `RemoteClusterConnection` (#22804)

    Some tests verify that all connection have been closed but due to the
    async / concurrent nature of `RemoteClusterConnection` there are situations
    where we notify listeners that trigger tests to finish before we actually
    closed all connections. The race is very very small and has no impact on the
    code correctness. This commit documents and improves the way we close
    connections to ensure test won't fail with false positives.

    Closes #22803