commit 8c3a4efce2882a8823df06b369e5d079765bed5b
Author: Jakob Buchgraber <buchgr@google.com>
Date:   Tue Feb 28 18:49:50 2017 +0000

    BEP: Add BuildFinished event.

    The build event protocol now emits a BuildFinished event at the end of a build. The event is a child of the BuildStarted event.

    The code changes were larger than expected, due to some refactoring in BuildEventStreamer. This was necessary as the BuildCompleteEvent now implements the BuildEvent interface and Guava's EventBus always invokes the most specialized subscriber method. Thus, the buildEvent(BuildEvent) and buildCompleted(BuildCompletedEvent) methods had to be merged.

    --
    Change-Id: Id0c2bd7220dc8ce03128b7126587e212ee8ce836
    Reviewed-on: https://cr.bazel.build/9053
    PiperOrigin-RevId: 148788582
    MOS_MIGRATED_REVID=148788582