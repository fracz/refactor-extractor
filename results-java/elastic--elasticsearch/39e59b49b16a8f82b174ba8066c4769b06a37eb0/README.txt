commit 39e59b49b16a8f82b174ba8066c4769b06a37eb0
Author: Simon Willnauer <simonw@apache.org>
Date:   Thu Jun 1 22:23:41 2017 +0200

    Extract a common base class for scroll executions (#24979)

    Today there is a lot of code duplication and different handling of errors
    in the two different scroll modes. Yet, it's not clear if we keep both of
    them but this simplification will help to further refactor this code to also
    add cross cluster search capabilities.

    This refactoring also fixes bugs when shards failed due to the node dropped out of the cluster in between scroll requests and failures during the fetch phase of the scroll. Both places where simply ignoring the failure and logging to debug. This can cause issues like #16555