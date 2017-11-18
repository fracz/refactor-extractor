commit 1a7391070f877e8418b44148ecc8f3664e8b85f3
Author: javanna <cavannaluca@gmail.com>
Date:   Wed Nov 25 17:49:02 2015 +0100

    Simulate api improvements

    Move ParsedSimulateRequest to SimulatePipelineRequest and remove Parser class in favor of static parse methods.
    Simplified execute methods in SimulateExecutionService.