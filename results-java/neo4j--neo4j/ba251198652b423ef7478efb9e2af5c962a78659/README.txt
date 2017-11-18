commit ba251198652b423ef7478efb9e2af5c962a78659
Author: lutovich <konstantin.lutovich@neotechnology.com>
Date:   Fri Jul 8 14:58:45 2016 +0200

    Differentiate between rollback and read-only for KTI id

    This commit makes KernelTransactionImplementation#closeTransaction() return
    different values when transaction was rolled back or read only. It also
    improves javadoc for KernelTransaction interface.