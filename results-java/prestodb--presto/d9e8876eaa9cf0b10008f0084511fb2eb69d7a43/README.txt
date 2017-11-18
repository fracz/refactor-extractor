commit d9e8876eaa9cf0b10008f0084511fb2eb69d7a43
Author: Raghav Sethi <raghavsethi@fb.com>
Date:   Mon Aug 22 16:43:26 2016 -0700

    Fix race in event listener test

    Changed TestEventListener to be single threaded, and added countdown
    latches to allow waiting for a predefined number of events. Also
    improved test to re-use query runner and session.