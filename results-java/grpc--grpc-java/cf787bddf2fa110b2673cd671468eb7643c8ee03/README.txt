commit cf787bddf2fa110b2673cd671468eb7643c8ee03
Author: Kun Zhang <zhangkun@google.com>
Date:   Fri Jan 29 17:25:42 2016 -0800

    DelayedClientTransport and fix TransportSet.shutdown() semantics.

    Always return a completed future from `TransportSet`. If a (real) transport has not been created (e.g., in reconnect back-off), a `DelayedClientTransport` will be returned.

    Eventually we will get rid of the transport futures everywhere, and have streams always __owned__ by some transports.

    DelayedClientTransport
    ----------------------

    After we get rid of the transport future, this is what `ClientCallImpl` and `LoadBalancer` get when a real transport has not been created yet. It buffers new streams and pings until `setTransport()` is called, after which point all buffered and future streams/pings are transferred to the real transport.

    If a buffered stream is cancelled, `DelayedClientTransport` will remove it from the buffer list, thus #1342 will be resolved after the larger refactoring is complete.

    This PR only makes `TransportSet` use `DelayedClientTransport`. Follow-up changes will be made to allow `LoadBalancer.pickTransport()` to return null, in which case `ManagedChannelImpl` will give `ClientCallImpl` a `DelayedClientTransport`.

    Changes to ClientTransport shutdown semantics
    ---------------------------------------------

    Previously when shutdown() is called, `ClientTransport` should not accept newStream(), and when all existing streams have been closed, `ClientTransport` is terminated. Only when a transport is terminated would a transport owner (e.g., `TransportSet`) remove the reference to it.

    `DelayedClientTransport` brings about a new case: when `setTransport()` is called, we switch to the real transport and no longer need the delayed transport. This is achieved by calling `shutdown()` on the delayed transport and letting it terminate. However, as the delayed transport has already been handed out to users, we would like `newStream()` to keep working for them, even though the delayed transport is already shut down and terminated.

    In order to make it easy to manage the life-cycle of `DelayedClientTransport`, we redefine the shutdown semantics of transport:
    - A transport can own a stream. Typically the transport owns the streams
      it creates, but there may be exceptions. `DelayedClientTransport` DOES
      NOT OWN the streams it returns from `newStream()` after `setTransport()`
      has been called. Instead, the ownership would be transferred to the
      real transport.
    - After `shutdown()` has been called, the transport stops owning new
      streams, and `newStream()` may still succeed. With this idea,
      `DelayedClientTransport`, even when terminated, will continue
      passing `newStream()` to the real transport.
    - When a transport is in shutdown state, and it doesn't own any stream,
      it then can enter terminated state.

    ManagedClientTransport / ClientTransport
    ----------------------------------------

    Remove life-cycle interfaces from `ClientTransport`, and put them in its subclass - `ManagedClientTransport`, with the same idea that we have `Channel` and `ManagedChannel`. Only the one who creates the transport will get `ManagedClientTransport` thus is able to start and shutdown the transport. The users of transport, e.g., `LoadBalancer`, can only get `ClientTransport` thus are not alter its state. This change clarifies the responsibility of transport life-cycle management.

    Fix TransportSet shutdown semantics
    -----------------------------------

    Currently, if `TransportSet.shutdown()` has been called, it no longer create new transports, which is wrong.

    The correct semantics of `TransportSet.shutdown()` should be:
    - Shutdown all transports, thus stop new streams being created on them
    - Stop `obtainActiveTransport()` from returning transports
    - Streams that already created, including those buffered in delayed transport, should continue. That means if delayed transport has buffered streams, we should let the existing reconnect task continue.