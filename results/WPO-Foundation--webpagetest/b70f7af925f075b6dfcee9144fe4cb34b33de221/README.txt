commit b70f7af925f075b6dfcee9144fe4cb34b33de221
Author: Stefan Burnicki <stefan.burnicki@iteratec.de>
Date:   Tue Jun 21 16:50:27 2016 +0200

    refactor: Splitted getRequests function to only depend on the paths

    This way, getRequestsForStep can be used for any files represented
    by localPaths. So this can be used for multistep in the future