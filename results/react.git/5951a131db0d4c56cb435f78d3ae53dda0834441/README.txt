commit 5951a131db0d4c56cb435f78d3ae53dda0834441
Author: Sebastian Markbage <sema@fb.com>
Date:   Mon Nov 17 15:53:26 2014 -0800

    Move ComponentEnvironment out of ReactComponent

    We currently have three DOM specific hooks that get injected. I move those
    out to ReactComponentEnvironment. The idea is to eventually remove this
    injection as the reconciler gets refactored.

    There is also a BackendIDOperation which is specific to the DOM component
    itself so I move this injection to be more specific to the DOMComponent.

    E.g. it makes sense for it to be injectable for cross-worker DOM operations
    but it doesn't make sense for ART components.