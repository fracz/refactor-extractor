commit a4b00c732b13aa1628161cb35d49463c5c39d38c
Author: Chris Beams <cbeams@vmware.com>
Date:   Fri May 25 17:23:01 2012 +0300

    Introduce BeanFactoryAnnotationUtils

    Commit 096693c46fba6e09b346a498b7002abd4d6540a9 refactored and
    deprecated TransactionAspectUtils, moving its #qualifiedBeanOfType
    and related methods into BeanFactoryUtils. This created a package cycle
    between beans.factory and beans.factory.annotation due to use of the
    beans.factory.annotation.Qualifier annotation in these methods.

    This commit breaks the package cycle by introducing
    beans.factory.annotation.BeanFactoryAnnotationUtils and moving these
    @Qualifier-related methods to it. It is intentionally similar in name
    and style to the familiar BeanFactoryUtils class for purposes of
    discoverability.

    There are no backward-compatibilty concerns associated with this change
    as the cycle was introduced, caught and now fixed before a release.

    Issue: SPR-6847