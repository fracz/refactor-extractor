commit ea172999bd6f0b6f5b05613040f730a03ea40a1d
Author: Ulf Adams <ulfjack@google.com>
Date:   Tue Feb 9 18:57:13 2016 +0000

    Skyframe-based loading phase runner: report errors if a package is in error.

    This is a bit odd - the legacy loading phase runner reports a loading error,
    but no target pattern error in keep_going mode, even though it's clearly an
    error in the referenced target itself, rather than in its transitive closure.

    This happens because the target pattern eval swallows such errors in keep_going
    mode, and doesn't even report the error. I tried changing that, but it's a
    fairly large refactoring, and that code path is dead if we switch to the new
    one.

    In the Skyframe-based implementation, both keep_going and nokeep_going paths
    now report a target pattern error. (Note that the new code can never report a
    loading error, because it doesn't perform transitive loading.)

    The corresponding test is moved from SkyframeLoadingAndAnalysisTest to
    LoadingPhaseRunnerTest - we don't need any integration test setup for that.

    --
    MOS_MIGRATED_REVID=114236897