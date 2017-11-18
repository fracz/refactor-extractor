commit f37efb42790c1da0ae858a2c6da4c809e0f9bd6f
Author: Rossen Stoyanchev <rstoyanchev@vmware.com>
Date:   Mon Apr 23 11:20:17 2012 -0400

    Polish Servlet async request processing

    * Clarify semantics and behavior of AsyncWebRequest methods in most cases
    making a best effort and not raising an exception if async processing
    has completed for example due to a timeout. The startAsync() method is
    still protected with various checks and will raise ISE under a number
    of conditions.
    * Return 503 (service unavailable) when requests time out.
    * Logging improvements.

    Issue: SPR-8517