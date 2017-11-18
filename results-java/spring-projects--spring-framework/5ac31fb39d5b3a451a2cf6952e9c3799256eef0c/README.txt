commit 5ac31fb39d5b3a451a2cf6952e9c3799256eef0c
Author: Brian Clozel <bclozel@pivotal.io>
Date:   Mon May 2 15:39:07 2016 +0200

    Refactor HTTP Range support with ResourceRegion

    Prior to this commit, the `ResourceHttpMessageConverter` would support
    all HTTP Range requests and `MethodProcessors` would "wrap" controller
    handler return values with a `HttpRangeResource` to support that use
    case in Controllers.

    This commit refactors that support in several ways:
    * a new ResourceRegion class has been introduced
    * a new, separate, ResourceRegionHttpMessageConverter handles the HTTP
    range use cases when serving static resources with the
    ResourceHttpRequestHandler
    * the support of HTTP range requests on Controller handlers has been
    removed until a better solution is found

    Issue: SPR-14221, SPR-13834