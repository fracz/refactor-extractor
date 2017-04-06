commit d0022e31d7135f4cace568b8f7daa8b08d640ec2
Merge: 5e3756f c0687cd
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Mar 6 17:23:04 2013 +0100

    merged branch Tobion/deprecation-trigger (PR #7266)

    This PR was merged into the 2.2 branch.

    Commits
    -------

    c0687cd remove() should not use deprecated getParent() so it does not trigger deprecation internally
    708c0d3 adjust routing tests to not use prefix in addCollection
    6180c5b add test for uniqueness of resources
    c0de07b added tests for addDefaults, addRequirements, addOptions
    0a1cfcd adjust RouteCollectionTest for the addCollection change and refactor the tests to only skip the part that really needs the config component
    ea694e4 added tests for remove() that wasnt covered yet and special route name
    9e2bcb5 refactor interator test that was still assuming a tree
    ceb9ab4 adjust tests to no use addPrefix with options
    2b8bf6b adjusted tests to not use RouteCollection::getPrefix
    acff735 [Routing] trigger deprecation warning for deprecated features that will be removed in 2.3

    Discussion
    ----------

    [2.2][Routing] Trigger deprecation and refactor tests to not use deprecated methods

    | Q             | A
    | ------------- | ---
    | Bug fix?      | [yes]
    | New feature?  | [no]
    | BC breaks?    | [no]
    | Deprecations? | [no]
    | Tests pass?   | [yes]
    | License       | MIT

    @fabpot please don't squash because it also added new tests