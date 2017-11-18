commit d41fe75f09fa09fc708d389572639511d745c20e
Author: Alon Albert <aalbert@google.com>
Date:   Wed Dec 22 11:07:54 2010 -0800

    Improved ignore-backoff handling
    Allow a non-epidited ignore-backoff op to pass through
    an expidited backed off op.

    To do this, I first refactored the complicated if statement:

                if (best == null
                        || ((bestSyncableIsUnknownAndNotARetry == syncableIsUnknownAndNotARetry)
                            ? (best.expedited == op.expedited
                               ? opRunTime < bestRunTime
                               : op.expedited)
                            : syncableIsUnknownAndNotARetry)) {
                    best = op;
                    bestSyncableIsUnknownAndNotARetry = syncableIsUnknownAndNotARetry;
                    bestRunTime = opRunTime;
                }

    Into a more readable:
                boolean setBest = false;
                if (best == null) {
                    setBest = true;
                } else if (bestSyncableIsUnknownAndNotARetry == syncableIsUnknownAndNotARetry) {
                    if (best.expedited == op.expedited) {
                        if (opRunTime < bestRunTime) {
                            //  if both have same level, earlier time wins
                            setBest = true;
                        }
                    } else {
                        if (op.expedited) {
                            setBest = true;
                        }
                    }
                } else {
                    if (syncableIsUnknownAndNotARetry) {
                        setBest = true;
                    }
                }
                if (setBest) {
                    best = op;
                    bestSyncableIsUnknownAndNotARetry = syncableIsUnknownAndNotARetry;
                    bestRunTime = opRunTime;
                }

    The refactoring was all done automatically with IntelliJ to avoid human error
    in the conversion.

    After verifying this code still behaved as expected including the error
    condition in the bug, I added handling for the cases when a non-expidited op
    may override an expedited op if certain conditions occur, specificaly, if the
    expidited op is backed off and the non-expidited op is not.

    Finally, refactored to make it testable and added tests and logging.

    Bug: 3128963

    Change-Id: I131cbcec6073ea5fe425f6b5aa88ca56c02b6598