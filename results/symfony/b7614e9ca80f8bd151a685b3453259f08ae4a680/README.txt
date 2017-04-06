commit b7614e9ca80f8bd151a685b3453259f08ae4a680
Merge: e769d7f aadefd3
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Jan 21 16:18:25 2013 +0100

    merged branch fabpot/content-renderer-refactoring (PR #6810)

    This PR was merged into the master branch.

    Commits
    -------

    aadefd3 [HttpKernel] refactored the HTTP content renderer to make it easier to extend

    Discussion
    ----------

    [HttpKernel] refactored the HTTP content renderer to make it easier to extend

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | kinda
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | n/a
    | License       | MIT
    | Doc PR        | n/a

    This makes the StreamedResponse logic reusable for other strategies and it also makes the RenderingStrategy interface less fuzzy about its contract.

    That also makes features like #4470 easier to implement from the outside.

    ---------------------------------------------------------------------------

    by stof at 2013-01-20T11:01:29Z

    :+1: