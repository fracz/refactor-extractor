commit 2b7a62906891b7a936412894f9ab78cdb69dc378
Author: Sam Brannen <sam@sambrannen.com>
Date:   Sat Jul 28 01:24:32 2012 +0200

    Support TransactionManagementConfigurer in the TCF

    Currently the Spring TestContext Framework looks up a
    PlatformTransactionManager bean named "transactionManager". The exact
    name of the bean can be overridden via @TransactionConfiguration or
    @Transactional; however, the bean will always be looked up 'by name'.

    The TransactionManagementConfigurer interface that was introduced in
    Spring 3.1 provides a programmatic approach to specifying the
    PlatformTransactionManager bean to be used for annotation-driven
    transaction management, and that bean is not required to be named
    "transactionManager". However, as of Spring 3.1.2, using the
    TransactionManagementConfigurer on a @Configuration class has no effect
    on how the TestContext framework looks up the transaction manager.
    Consequently, if an explicit name or qualifier has not been specified,
    the bean must be named "transactionManager" in order for a transactional
    integration test to work.

    This commit addresses this issue by refactoring the
    TransactionalTestExecutionListener so that it looks up and delegates to
    a single TransactionManagementConfigurer as part of the algorithm for
    determining the transaction manager.

    Issue: SPR-9604