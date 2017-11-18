commit 1af28efe39d8b141f10779518e794d74d29972f1
Author: Juergen Hoeller <jhoeller@vmware.com>
Date:   Sun Feb 10 18:06:50 2013 +0100

    @Transactional in AspectJ mode works with CallbackPreferringPlatformTransactionManager (WebSphere) as well

    Effectively, AbstractTransactionAspect got refactored into around advice, reusing former TransactionInterceptor code which now lives in slightly generalized form in TransactionAspectSupport, and using a workaround for rethrowing checked exceptions.

    Issue: SPR-9268