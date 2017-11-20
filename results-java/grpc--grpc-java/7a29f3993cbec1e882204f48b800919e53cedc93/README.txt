commit 7a29f3993cbec1e882204f48b800919e53cedc93
Author: Kun Zhang <zhangkun@google.com>
Date:   Fri Feb 12 23:54:30 2016 -0800

    Pass transports instead of futures of transports for new calls.

    This PR finishes the refactoring started by #1395. Resolves #1342.

    Major changes:

    - All interfaces that return `ListenableFuture<T>` now return `T`
    - Stop passing `null` for transports after shut down. Pass
      `FailingClientTransport` instead. This simplifies the interface
      semantics of interfaces that return a transport, as well as their
      callers because they no longer need to check for `null`.
    - Add two methods to `TransportManager`:
     - `createInterimTransport()` takes the place of `BlankFutureProvider`
      in `LoadBalancer` implementations.
     - `createFailingTransport()` takes the place of errors in the `Future`
      when `LoadBalancer` implementations propagate errors to the caller.
    - `createInterimTransport()` creates a `DelayedClientTransport` tracked
      by `ManagedChannelImpl`, which will not terminate until all
      `DelayedClientTransport`s, in addition to the `TransportSet`s, are
      terminated.
    - Removed the transport argument from the ready and shutdown callbacks,
      because if `LoadBalancer` was given a delayed transport earlier, it
      will get the real transport's shutdown event, thus won't be able to
      match the two through identity comparison, which beats the purpose of
      passing the transport.