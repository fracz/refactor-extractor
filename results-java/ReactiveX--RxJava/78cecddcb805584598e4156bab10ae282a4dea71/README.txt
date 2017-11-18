commit 78cecddcb805584598e4156bab10ae282a4dea71
Author: Ben Christensen <benjchristensen@netflix.com>
Date:   Tue Sep 10 12:39:01 2013 -0700

    Operators: throttleWithTimeout, throttleFirst, throttleLast

    - javadocs explaining differences
    - link between throttleLast and sample (aliase)
    - refactored throttleFirst to be a more efficient implementations
    - concurrency changes to throttleWithTimeout