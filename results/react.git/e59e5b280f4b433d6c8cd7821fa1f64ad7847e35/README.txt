commit e59e5b280f4b433d6c8cd7821fa1f64ad7847e35
Author: Sebastian Markbage <sema@fb.com>
Date:   Fri Oct 7 15:46:43 2016 -0700

    Invoke all null ref calls before any new ref calls

    This reorganizes the two commit passes so that all host
    environment mutations happens before any life-cycles. That means
    that the tree is guaranteed to be in a consistent state at that
    point so that you can read layout etc.

    This also lets us to detach all refs in the same pass so that
    when they get invoked with new instances, that happens after it
    has been reset.