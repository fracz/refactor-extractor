commit be53a80657a7e613f148c9f3a7fbacc74eb0fc77
Author: Chris Beams <cbeams@vmware.com>
Date:   Fri Dec 19 21:58:42 2008 +0000

    moved ApplicationContext-dependent .aop.* unit tests from .testsuite -> .context
    in the process, identified and refactored two genuine integration tests (AopNamespaceHandlerScopeIntegrationTests, AdvisorAutoProxyCreatorIntegrationTests), which will remain in .testsuite due to broad-ranging dependencies