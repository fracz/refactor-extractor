commit 8c3054b4e4c67bd78c7526abe3cd94457e1cc9ec
Author: Jacob Hansson <jakewins@gmail.com>
Date:   Tue Jun 28 20:25:15 2016 -0500

    Only session worker thread is allowed to write to outbound channel

    Bolt can be seen as implemented in two layers - one syntactic layer dealing
    with protocol format, chunking, message fragmentation and all that, and one
    semantic layer dealing with what the abstract messages in Bolt mean.

    This is implemented as two separate layers of threads as well - one set
    of threads deal with inbound IO, IO Threads, and another set of threads
    deal with executing the semantic work, Session Worker Threads.

    The model is that all traffic moves through the same pipeline:

        (IO Thread) -[queue]-> (Worker Thread) -[queue]-> (IO Thread)

    Meaning; there's no way for the IO Thread that receives inbound data
    to immediately respond back on its own; it can *only* forward messages to
    the Worker Thread, which then acts on them and produces outbound messages
    for the IO layer again.

    The problem this commit solves was a violation of this, where an IO Thread
    would write directly back to the client in the case of a fatal error
    while processing a request. This caused illegal states to occur, because the
    worker thread could very likely be telling the IO Layer to write something
    else at the same time.

    This resolves the violation by introducing a special "externalError" signal
    that the IO layer can send, which basically is just a roundabout way of
    sending an error back to the client.

    Overall, the area of code that this touches - MessageHandler, Session,
    Callback and TransportBridge - are a set of interfaces that are very hard
    to find how they interact. The design is the result of an original approach
    that did not quite fit and under-refactoring as we learned that.

    Hence; I'd love to refactor this further, but for now, this resolves the
    immediate issue and removes the only violation we had of this message flow
    approach.