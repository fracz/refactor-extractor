commit f173fe53cddd9eccb55157315ff256caf2016a56
Author: Martin Furmanski <martin.furmanski@neotechnology.com>
Date:   Wed Jun 8 17:24:34 2016 +0200

    core-edge: refactor log entry supplier and batcher

    Pushed most of the logic for retreiving log entries from in-memory and
    on-disk structures to LogEntrySupplier and the logic for batching to
    OperationBatcher. The code reads easier now, I claim.