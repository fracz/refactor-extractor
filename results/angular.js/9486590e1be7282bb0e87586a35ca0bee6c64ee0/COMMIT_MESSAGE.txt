commit 9486590e1be7282bb0e87586a35ca0bee6c64ee0
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Sun Feb 19 12:31:11 2012 -0800

    refactor(ng:view) Make $route scope agnostic, add $contentLoaded event

    Problems:

    - controller was instantiated immediately on $afterRouteChange (even if no content), that's
    different compare to ng:controller, which instantiates controllers after compiling
    - route listened on current scope ($afterRouteChange), so if you were listening on $rootScope
    ($afterRouteChange), you get called first and current.scope === undefined, which is flaky
    - route handles scope destroying, but scope is created by ng:view
    - route fires after/before route change even if there is no route (when no otherwise specified)

    Solution:

    - route has no idea about scope, whole scope business moved to ng:view (creating/destroying)
    - scope is created (and controller instantiated) AFTER compiling the content
    - that means on $afterRouteChange - there is no scope yet (current.scope === undefined)
    - added $contentLoaded event fired by ng:view, after linking the content