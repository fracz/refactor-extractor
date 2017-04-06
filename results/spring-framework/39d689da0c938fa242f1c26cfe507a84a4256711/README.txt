commit 39d689da0c938fa242f1c26cfe507a84a4256711
Author: Brian Clozel <bclozel@pivotal.io>
Date:   Fri Jun 26 15:28:59 2015 +0200

    Fix conditional requests support for HttpEntity

    Prior to this commit, `HttpEntityMethodProcessor` would rely on
    `ServletWebRequest` to process conditional requests and with incoming
    `"If-Modified-Since"` / `"If-None-Match"` request headers.

    This approach is problematic since in that class:

    * response is wrapped in a `ServletServerHttpResponse`
    * this wrapped response does not write response headers right away
    * `ServletWebRequest.checkNotModified` methods can't apply their
    logic with incomplete response headers

    This solution adds some minimal code duplication and applies
    the conditional request logic within the Processor.

    A possible alternative would be to improve the
    `ServletServerHttpResponse$ServletResponseHttpHeaders` implementation
    with write methods - but this solution would only work for Servlet 3.x
    applications.

    Issue: SPR-13090