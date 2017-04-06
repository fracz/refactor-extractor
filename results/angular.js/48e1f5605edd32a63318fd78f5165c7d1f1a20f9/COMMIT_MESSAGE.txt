commit 48e1f5605edd32a63318fd78f5165c7d1f1a20f9
Author: Peter Bacon Darwin <pete@bacondarwin.com>
Date:   Thu Jun 18 08:33:02 2015 +0100

    fix(orderBy): ensure correct ordering with arrays of objects and no predicate

    By refactoring to use a Schwartzian transform, we can ensure that objects
    with no custom `toString` or `toValue` methods are just ordered using
    their position in the original collection.

    Closes #11866
    Closes #11312
    Closes #4282