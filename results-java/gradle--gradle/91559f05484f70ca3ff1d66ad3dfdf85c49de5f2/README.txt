commit 91559f05484f70ca3ff1d66ad3dfdf85c49de5f2
Author: Luke Daley <ld@ldaley.com>
Date:   Fri Jul 27 13:25:17 2012 +0100

    Another attempt at taming test logging.

    I chose to duplicate some of LogbackLoggingConfigurer instead of refactoring for reuse as I'm not confident in changing LogbackLoggingConfigurer and the requirements are slightly different for testing.

    The intention was to use a test rule to reset the logging before each test. There's some awkwardness with this in that some of the logging comes from other rules, which may run before the logging reset rule. This means that rules that emit logging need to explicitly reset logging before doing so.

    A better approach might be to have a baseline logback.xml on the classpath and then reset the configuration _after_ each test (in case it fiddles with the config).