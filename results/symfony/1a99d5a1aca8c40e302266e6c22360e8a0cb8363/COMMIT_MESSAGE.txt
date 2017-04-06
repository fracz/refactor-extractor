commit 1a99d5a1aca8c40e302266e6c22360e8a0cb8363
Merge: 9d6c5d8 6211205
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Aug 29 11:07:47 2014 +0200

    minor #11762 [DependencyInjection] Enhance tests for class Container (kerdany)

    This PR was squashed before being merged into the 2.6-dev branch (closes #11762).

    Discussion
    ----------

    [DependencyInjection] Enhance tests for class Container

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | no
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    Added missing tests/assertions:
    - Assertions that ->compile() resolves parameter bag.
    - Asserton that ->getServiceIds() shows ids defined by set() after ids defined by get*Service() methods.
    - Test that ->set() automatically calls synchronize*Service if defined. Updated associated fixtures in the ProjectServiceContainer class definition.
    - Assertion that ->get() is case insensitive.
    - Assertion that leaving an inner scope with active child scope, to an outer scope, deactivates/resets the child scope(s).
    - Test that entering a child scope recursively resets the inner scope.
    - Test that a scope can not be entered before it's added first.
    - Test that a scope can not be entered before adding and entering the parent scope first (for non container scopes).
    - Test for underscore().

    Other changes:
    - Added missing messages in some assertions.
    - Moved testGetThrowsException*() methods close to testGet*() tests.
    - Renamed variable(s) '$services' (referencing the $sc->scopedServices field) to $scoped, so as not to confuse it with the global scope map.
    - Minor refactoring in class Container for code consistency and reducing redundancy.

    Commits
    -------

    6211205 [DependencyInjection] Enhance tests for class Container