commit 34434cf528184290ce25fd11066ff8a3fa760519
Author: Jason Bedard <jason+github@jbedard.ca>
Date:   Tue Jun 7 22:10:46 2016 -0700

    refactor($q): separate Promise from Deferred

    Closes #15064

    BREAKING CHANGE:

    Previously, the `Deferred` object returned by `$q.defer()` delegated the
    `resolve()`, `reject()` and `notify()` methods to `Deferred.prototype`. Thus, it
    was possible to modify `Deferred.prototype` and have the changes reflect to all
    `Deferred` objects.

    This commit removes that delegation, so modifying the above three methods on
    `Deferred.prototype` will no longer have an effect on `Deferred` objects.