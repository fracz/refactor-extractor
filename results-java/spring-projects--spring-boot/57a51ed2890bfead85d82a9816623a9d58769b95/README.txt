commit 57a51ed2890bfead85d82a9816623a9d58769b95
Author: Andy Wilkinson <awilkinson@pivotal.io>
Date:   Mon May 11 10:34:39 2015 +0100

    Update configuration of Jolokia’s AgentServlet to support CORS

    Spring Framework 4.2 introduces improved support for CORS. Notably this
    means that a DispatcherServlet will now process an OPTIONS request if
    it contains an Origin header, without having to enable OPTIONS request
    dispatching for every endpoint.

    This commit takes advantage of these changes in Spring Framework 4.2 by
    configuring the controller that wraps Jolokia’s AgentServlet to handle
    OPTIONS requests. This allows Jolokia’s CORS support to be configured
    using Jolokia’s standard configuration, as described in section 4.1.5
    of the Jolokia documentation [1].

    Closes gh-1987

    [1] https://jolokia.org/reference/html/security.html