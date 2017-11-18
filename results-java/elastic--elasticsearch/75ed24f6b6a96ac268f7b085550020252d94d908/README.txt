commit 75ed24f6b6a96ac268f7b085550020252d94d908
Author: Shay Banon <kimchy@gmail.com>
Date:   Thu Jul 10 18:02:47 2014 +0200

    Improve handling of failed primary replica handling
    Out of #6808, we improved the handling of a primary failing to make sure replicas that are initializing are properly failed as well. After double checking it, it has 2 problems, the first, if the same shard routing is failed again, there is no protection that we don't apply the failure (which we do in failed shard cases), and the other was that we already tried to handle it (wrongly) in the elect primary method.
    This change fixes the handling to work correctly in the elect primary method, and adds unit tests to verify the behavior
    closes #6816