commit 096693c46fba6e09b346a498b7002abd4d6540a9
Author: Chris Beams <cbeams@vmware.com>
Date:   Sat May 19 19:30:58 2012 +0300

    Refactor and deprecate TransactionAspectUtils

    TransactionAspectUtils contains a number of methods useful in
    retrieving a bean by type+qualifier. These methods are functionally
    general-purpose save for the hard coding of PlatformTransactionManager
    class literals throughout.

    This commit generifies these methods and moves them into
    BeanFactoryUtils primarily in anticipation of their use by async method
    execution interceptors and aspects when performing lookups for qualified
    executor beans e.g. via @Async("qualifier").

    The public API of TransactionAspectUtils remains backward compatible;
    all methods within have been deprecated, and all calls to those methods
    throughout the framework refactored to use the new BeanFactoryUtils
    variants instead.