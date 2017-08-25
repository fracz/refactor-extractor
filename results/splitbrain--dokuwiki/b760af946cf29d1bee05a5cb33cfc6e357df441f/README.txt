commit b760af946cf29d1bee05a5cb33cfc6e357df441f
Author: Michael Hamann <michael@content-space.de>
Date:   Sat Oct 15 14:54:10 2011 +0200

    Send a 401 Unauthorized header in XML-RPC when access is denied

    This is far from perfect but should solve most issues in the recommended
    configuration where only authorized users have access. Sending proper
    status codes should be implemented when the API implementation
    refactoring is done.