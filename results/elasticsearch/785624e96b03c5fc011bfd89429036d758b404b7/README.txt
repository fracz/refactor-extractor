commit 785624e96b03c5fc011bfd89429036d758b404b7
Author: Jason Tedor <jason@tedor.me>
Date:   Thu Aug 4 11:22:13 2016 -0400

    Restore interrupted status on when closing client

    When closing a transport client that depends on Netty 4, interrupted
    exceptions can be thrown while shutting down some Netty threads. This
    commit refactors the handling of these exceptions to finish shutting
    down and then just restore the interrupted status.