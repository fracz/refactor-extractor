commit e7eed5874f0e7392789e716587ad6e071d98688a
Author: Andrew Clark <acdlite@me.com>
Date:   Wed Nov 30 16:54:20 2016 -0800

    [Fiber] New error boundary semantics (#8304)

    * Exit early in scheduleUpdate if a node's priority matches

    This is a performance optimization and is unobservable. However, it
    helps protect against regressions on the following invariants on which
    it relies:

    - The priority of a fiber is greater than or equal to the priority of
    all its descendent fibers.
    - If a tree has pending work priority, its root is scheduled.

    * New error boundary semantics

    - Recovering from an error boundary no longer uses Task priority by
    default. The work is scheduled using whatever priority created the
    error.
    - Error handling is now a side-effect that happens during the
    commit phase.
    - The default behavior of an error boundary is to render null. Errors
    do not propagate except when an boundary fails. Conceptually, this would
    be like throwing an error from a catch block.
    - A host container is treated like a no-op error boundary. The first
    error captured by a host container is thrown at the end of the batch.
    Like with normal error boundaries, the entire tree is unmounted.

    * Fix broken setState callback test

    * Add test for "unwinding" context when an error interrupts rendering

    * Switch over primary effect types only

    This avoids the need to create an export for every combination of bits.

    * Only continue the work loop if the error was successfully captured

    * Add more tests for incremental error handling

    These tests are currently failing:

      ✕ catches render error in a boundary during partial deferred mounting
      ✕ catches render error in a boundary during animation mounting
      ✕ propagates an error from a noop error boundary during full deferred mounting
      ✕ propagates an error from a noop error boundary during partial deferred mounting
      ✕ propagates an error from a noop error boundary during animation mounting

    The observed behavior is that unstable_handleError() unexpected gets called twice:

          "ErrorBoundary render success",
          "BrokenRender",
          "ErrorBoundary unstable_handleError",
      +   "ErrorBoundary render success",
      +   "BrokenRender",
      +   "ErrorBoundary unstable_handleError",
          "ErrorBoundary render error"

    * Verify batched updates get scheduled despite errors

    * Add try/catch/finally blocks around commit phase passes

    We'll consolidate all these blocks in a future PR that refactors the
    commit phase to be separate from the perform work loop.

    * NoopBoundary -> RethrowBoundary

    * Only throw uncaught error once there is no more work to perform

    * Remove outdated comment

    It was fixed in #8451.

    * Record tests

    * Always reset nextUnitOfWork on error

    This is important so that the test at the end of performAndHandleErrors() knows it's safe to rethrow.

    * Add a passing test for unmounting behavior on crashed tree

    * Top-level errors

    An error thrown from a host container should be "captured" by the host
    container itself

    * Remove outdated comment

    * Separate Rethrow and Noop scenarios in boundary tests

    * Move try block outside the commit loops