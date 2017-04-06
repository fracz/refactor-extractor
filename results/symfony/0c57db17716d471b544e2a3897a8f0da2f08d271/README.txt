commit 0c57db17716d471b544e2a3897a8f0da2f08d271
Merge: 3469713 9307f5b
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Apr 11 15:53:10 2012 +0200

    merged branch vicb/routing_improvements (PR #3876)

    Commits
    -------

    9307f5b [Routing] Implement bug fixes and enhancements

    Discussion
    ----------

    [Routing] Implement bug fixes and enhancements (from @Tobion)

    This is mainly #3754 with some minor formatting changes.

    Original PR message from @Tobion:

    Here a list of what is fixed. Tests pass.

    1. The `RouteCollection` states

        > it overrides existing routes with the same name defined in the instance or its children and parents.

        But this is not true for `->addCollection()` but only for `->add()`. addCollection does not remove routes with the same name in its parents (only in its children). I don't think this is on purpose.
        So I fixed it by making sure there can only be one route per name in all connected collections. This way we can also simplify `->get()` and `->remove()` and improve performance since we can stop iterating recursively when we found the first and only route with a given name.
        See `testUniqueRouteWithGivenName` that fails in the old code but works now.

    2. There was an bug with `$collection->addPrefix('0');` that didn't apply the starting slash. Fixed and test case added.

    3. There is an issue with `->get()` that I also think is not intended. Currently it allows to access a sub-RouteCollection by specifing $name as array integer index. But according to the PHPdoc you should only be allowed to receive a Route instance and not a RouteCollection.
        See `testGet` that has a failing test case. I fixed this behavior.

    4. Then I recognized that `->addCollection` depended on the order of applying them. So

            $collection1->addCollection($collection2, '/b');
            $collection2->addCollection($collection3, '/c');
            $rootCollection->addCollection($collection1, '/a');

        had a different pattern result from

            $collection2->addCollection($collection3, '/c');
            $collection1->addCollection($collection2, '/b');
            $rootCollection->addCollection($collection1, '/a');

        Fixed and test case added. See `testPatternDoesNotChangeWhenDefinitionOrderChanges`.

    5. PHP could have ended in an infinite loop when one tried to add an existing RouteCollection to the tree. Fixed by throwing an exception when this situation is detected. See tests `testCannotSelfJoinCollection` and `testCannotAddExistingCollectionToTree`.

    6. I made `setParent()` private because its not useful outside the class itself. And `remove()` also removes the route from its parents. Added public `getRoot()` method.

    7. The `Route` class throwed a PHP warning when trying to set an empty requirement.

    8. Fixed issue #3777. See discussion there for more info. I fixed it by removing the over-optimization that was introduced in 91f4097a0905e but didn't work properly. One cannot reorder the route definitions, as is was done, because then the wrong route might me matched before the correct one. If one really wanted to do that, it would require to calculate the intersection of two regular expressions to determine if they can be grouped together (a tool that would also be useful to check whether a route is unreachable, see discussion in #3678) We can only safely optimize routes with a static prefix within a RouteCollection, not across multiple RouteCollections with different parents.  This is especially true when using variables and regular expressions requirements.
    I could however apply an optimization that was missing yet: Collections with a single route were missing the static prefix optimization with `0 === strpos()`.

    9. Fixed an issue where the `PhpMatcherDumper` would not apply the optimization if the root collection to be dumped has a prefix itself. For this I had to rewrite `compileRoutes`. It is also much easier to understand now. Addionally I added many comments and PHPdoc because complex recursive methods like this are still hard to grasp.
    I added a test case for this (`url_matcher3.php`).

    10. Fix that `Route::compile` needs to recompile a route once it is modified. Otherwise we have a wrong result. Test case added.