commit 93aea2e9b53120140a9a154971ec18bf6c1412f8
Author: philwo <philwo@google.com>
Date:   Thu Apr 27 23:29:14 2017 +0200

    worker: Do not check if the process is still alive prior to using it.

    This might sound strange at first, but the reasoning is this: A worker should never simply exit. Bazel controls the lifetime of its subprocesses, so a worker quitting is considered a failure (also because it cannot be distinguished from a crash).

    With that set, we can improve two things:
    - Bazel will now only notice that a worker crashed / quit when it tries to use one during a build. This is also the only time when we can print error messages to the user. Earlier we might have noticed that a worker crashed during validation, but had no mechanism to alert the user to this, because this wasn't necessarily during a build.
    - This also fixes a race condition where a worker is still alive during validation, then quits, then the WorkerSpawnStrategy tries to send a WorkRequest, which fails, triggering an IOException.

    This fixes the flaky test test_worker_restarts_after_exit (which is now called test_build_fails_when_worker_exits).

    Part of #2855.

    RELNOTES: Bazel will no longer gracefully restart workers that crashed / quit, instead this triggers a build failure.
    PiperOrigin-RevId: 154470257