commit e86e27dcdef0d6757f2892cddcd2795758dfbff5
Author: Stefan Burnicki <stefan.burnicki@iteratec.de>
Date:   Mon Jun 20 15:26:24 2016 +0200

    refactor: Extract GetVisualProcess method which only deals with TestPaths

    Prerequisites for multistep, as TestPaths might also represent paths
    of a specific step, not only a run.