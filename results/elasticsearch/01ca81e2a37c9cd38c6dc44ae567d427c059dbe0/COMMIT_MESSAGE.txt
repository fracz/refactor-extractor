commit 01ca81e2a37c9cd38c6dc44ae567d427c059dbe0
Author: Shay Banon <kimchy@gmail.com>
Date:   Fri Jul 11 09:11:51 2014 +0200

    Improve handling of failed primary replica handling
    Out of #6808, we improved the handling of a primary failing to make sure replicas that are initializing are properly failed as well. After double checking it, it has 2 problems, the first, if the same shard routing is failed again, there is no protection that we don't apply the failure (which we do in failed shard cases), and the other was that we already tried to handle it (wrongly) in the elect primary method.
    This change fixes the handling to work correctly in the elect primary method, and adds unit tests to verify the behavior
    The change also expose a problem in our handling of replica shards that stay initializing during primary failure and electing another replica shard as primary, where we need to cancel its ongoing recovery to make sure it re-starts from the new elected primary
    closes #6825