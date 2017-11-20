commit 1796531d6bb1fc28f8e644cd546cabf1d241a7dd
Author: Todd L. Montgomery <todd.montgomery@kaazing.com>
Date:   Thu Aug 7 20:51:10 2014 -0700

    [Java]: #4 refactored cleanup of publications in driver. Publications linger for a while after being firest removed or timed out due to client keepalive and then flushed. Added close command for sender queue to coordinate unmap on Sender thread. However, could still theoretically happen.