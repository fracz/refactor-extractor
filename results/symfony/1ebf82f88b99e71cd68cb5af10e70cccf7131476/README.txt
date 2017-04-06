commit 1ebf82f88b99e71cd68cb5af10e70cccf7131476
Merge: 3696ffc 187aeee
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Sep 24 11:28:39 2014 +0200

    feature #12008 [DependencyInjection] Add a new Syntax to define factories as callables (realityking, fabpot)

    This PR was merged into the 2.6-dev branch.

    Discussion
    ----------

    [DependencyInjection] Add a new Syntax to define factories as callables

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | yes
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    From the original PR #9839:

    "This pull requests adds a new syntax to define factories based on the syntax for configurators. This is more flexible than the old syntax (factoryMethod and either of factoryClass or factoryService), as it also allows for functions as factories.

    Since the service is now a Reference to a Definition it also allows us to inline factories for a small performance improvement and better encapsulation.

    Lastly this prevents a bug where a private factory is simple removed because it's not referenced in the graph.

    I did not change any of the existing definitions (there's one use of a factory in FrameworkBundle) or automatically use the new internal representation when parsing YAML or XML definitions because this could introduce subtle B/C issues.
    "

    Commits
    -------

    187aeee fixed CS
    bd8531d added a new Syntax to define factories as callables.