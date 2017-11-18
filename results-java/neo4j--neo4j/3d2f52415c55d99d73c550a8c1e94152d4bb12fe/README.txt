commit 3d2f52415c55d99d73c550a8c1e94152d4bb12fe
Author: Martin Furmanski <martin.furmanski@neotechnology.com>
Date:   Fri Apr 7 15:02:47 2017 +0200

    refactor core state and related

    The names, roles, responsibilities and interactions of these classes has
    been less than optimal for some time. It has simply grown organically to
    a point where it has to be refactored, so this commit tries to clear
    things up a bit now that things are clearer.

    The top level class previously called CoreState can now be said to have
    been split up into two other top level classes dealing with parts of the top
    level entrypoint functionality that this class previously had. This
    functionality is now in RaftMessageHandler and CoreSnapshotService
    respectively. The responsibilities should be somewhat clear from the
    names.

    The access to the core state itself is all that is left in the CoreState
    class, exposing things like flush().

    The command application process had a messy setup of its synchronization
    externally and internally. It is now simplified for external users by
    exposing two calls, pause and resume, instead of what was previously
    a single sync() call relying on the operation to be performed under the
    critical section to be itself under the same monitor or at least
    other concurrent conflicting operations to be blocked by it. A less
    than ideal design. The pause/resume system is reference counting
    and not until the last outstanding call to resume will the applier
    actually resume.

    The snapshot service uses the pause/resume functionality as does
    the state/store downloader.

    A lot of code has moved around but not a lot of the code meat has
    changed. It should be easier now to reason about correctness,
    interactions and the design.

    This refactoring was taken on now because of the occurrence of a rare
    deadlock between the applier thread and its wrapping class, the
    CommandApplicationProcess. These two classes now still exist but the
    applier now has all its state and the synchronization of it wrapped in
    an internal class called ApplierState. This allows the extra flexibility
    needed to avoid the deadlock.