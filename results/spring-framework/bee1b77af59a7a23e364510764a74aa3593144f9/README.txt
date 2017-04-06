commit bee1b77af59a7a23e364510764a74aa3593144f9
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Tue Apr 12 09:06:48 2016 +0200

    Manage asynchronous EventListener with replies

    This commit makes sure to reject an `@EventListener` annotated method
    that also uses `@Async`. In such scenario, the method is invoked in a
    separate thread and the infrastructure has no handle on the actual reply,
    if any.

    The documentation has been improved to refer to that scenario.

    Issue: SPR-14113