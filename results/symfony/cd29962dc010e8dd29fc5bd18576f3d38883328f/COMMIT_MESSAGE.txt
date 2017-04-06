commit cd29962dc010e8dd29fc5bd18576f3d38883328f
Merge: 7e175ef d6d462a
Author: Bernhard Schussek <bschussek@gmail.com>
Date:   Mon Jul 28 16:51:00 2014 +0200

    minor #11463 [Validator] prevent unnecessary calls inside ExecutionContext (Tobion)

    This PR was merged into the 2.5 branch.

    Discussion
    ----------

    [Validator] prevent unnecessary calls inside ExecutionContext

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT

    Small performance improvement by preventing calls to `PropertyPath::append($this->propertyPath, $subPath)` when not needed.

    Commits
    -------

    d6d462a [Validator] do not call getter inside ExecutionContext to prevent unnecessary calls