commit 2f1af552a94e1b04fa9b75e40a78bb95518c22c3
Author: Simon Willnauer <simonw@apache.org>
Date:   Wed Mar 23 10:11:50 2016 +0100

    Bring back operation rollback on unexpected mapping change during recovery

    We lost some accounting code in the translog recover code during refactoring
    which triggers a very rare assertion. If we fail on a recovery target with an
    illegal mapping update (which can happen if the clusterstate is behind), then
    we miss to rollback the # of processed ops in that batch and once we resume
    the batch we trip an assertion that the stats are off.

    This commit brings back the code lost in 8bc2332d9ab028a5415a9606cd349790d3f5dc99
    and improves the comment that explains why we need this rollback logic.