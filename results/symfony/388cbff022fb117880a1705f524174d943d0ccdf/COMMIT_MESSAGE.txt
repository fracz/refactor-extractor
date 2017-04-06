commit 388cbff022fb117880a1705f524174d943d0ccdf
Merge: 88ea842 e54d749
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Oct 19 07:56:40 2012 +0200

    merged branch arnaud-lb/routing-php-dumper-simplification (PR #5734)

    This PR was merged into the master branch.

    Commits
    -------

    e54d749 [Routing] Simplified php matcher dumper (and optimized generated matcher)

    Discussion
    ----------

    [Routing] Simplified php matcher dumper (and optimized generated matcher)

    Bug fix: no
    Feature addition: no
    Related: #3378
    Backwards compatibility break: no
    Symfony2 tests pass: yes

    This simplifies the php matcher dumper by allowing the dumper to re-organize routes in the dumper's own structure.

    As a result, dumping is made a little simpler. This is also helps much for my hostname-based routes PR #3378.

    Reorganizing routes also allows to find more optimization opportunities:

    Currently the dumper wraps some collections of routes in a `if (0 === strpos($pathinfo, '/someprefix')` if the collection has user-defined prefix, and if it contains more than one direct child Route. This can miss many optimization opportunities.

    The PR changes this by building a prefix tree of routes based on the static prefix extracted from routes' patterns. Then every leave having a prefix and more than one child (route or collection) will be wrapped in a `if` statement.

    Example:

    ```
    // No explicit prefix is specified
    @Route("/cafe")
    @Route("/cacao")
    @Route("/coca")
    ```

    is compiled like this:

    ```
    if (url starts with /c) {
        if (url starts with /ca) {
            // test route "/cafe"
            // test route "/cacao"
        }
        // test route "/coca"
    }
    ```

    Some tests have many white space changes, much more easier to review [here](https://github.com/symfony/symfony/pull/5734/files?w=1)

    ---------------------------------------------------------------------------

    by Tobion at 2012-10-13T02:27:54Z

    I'm not sure if adding these specific classes just for dumping is the best implementation because they duplicate some logic and this optimization should also work out-of-the-box with the standard RouteCollection etc.
    What I have in mind is a new method in RouteCollection like `RouteCollection::optimizeTree` that returns a new RouteCollection with the Routes that includes these optimization you do here. So there would probably be no need for the new classes.

    It think it requires some changes in RouteCollection like the handling of prefix that must start with a slash currently, which is too restrictive. But it should be possible.

    ---------------------------------------------------------------------------

    by arnaud-lb at 2012-10-13T12:52:32Z

    @Tobion

    > I'm not sure if adding these specific classes just for dumping is the best implementation because they duplicate some logic and this optimization should also work out-of-the-box with the standard RouteCollection etc.

    I think RouteCollection and DumperCollection do not share the same concerns; and RouteCollection does things that don't allow to reorganize routes freely. For instance when adding a collection to a RouteCollection this changes all the child routes' prefix, requirements, options, etc. When setting a collection's prefix, this prepends the prefix to every child route's pattern, etc.

    ---------------------------------------------------------------------------

    by arnaud-lb at 2012-10-15T08:50:23Z

    squashed the CS commits

    ---------------------------------------------------------------------------

    by arnaud-lb at 2012-10-15T13:50:16Z

    @fabpot @Tobion this PR is ready to be merged if everyone agrees

    ---------------------------------------------------------------------------

    by Tobion at 2012-10-16T18:10:36Z

    When the above is fixed, I think it's good to be merged.

    ---------------------------------------------------------------------------

    by arnaud-lb at 2012-10-17T08:40:20Z

    Fixed; thanks @Tobion @stof for your reviews

    ---------------------------------------------------------------------------

    by Tobion at 2012-10-19T03:30:10Z

    @arnaud-lb could you please test whether your PR fixes #5780 as a side-effect?
    I can image that it's fixed because you use `$route->compile()->getStaticPrefix();` for prefix optimization.
    If it's fixed please add a test case. If not, that's fine, and we need to fix it in another PR.