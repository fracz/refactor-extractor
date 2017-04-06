commit 2da2a44382af2b9af85354ae5a381216534283d2
Merge: 5885547 0706d18
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Aug 30 09:34:36 2012 +0200

    merged branch Tobion/strictrequirements (PR #5181)

    Commits
    -------

    0706d18 [Routing] fixed 4 bugs in the UrlGenerator

    Discussion
    ----------

    [Routing] UrlGenerator: fixed missing query param and some ignored requirements

    This was pretty hard to figure out. I could fix 4 bugs and refactor the code to safe 2 variables and several assignments. Sorry for doing this in one commit, but they were highly interdependent.
    See the added tests for what was fixed. The most obvious bug was that a query param was ignored if it had by accident the same name as a default param (but wasn't used in the path).
    In 3 cases it generated the wrong URL that wouldn't match this route. The generator wrongly ignored either the requirements or the passed parameter. I had to adjust one test that was asserting something wrong (see comments).

    ---------------------------------------------------------------------------

    by Tobion at 2012-08-13T14:22:35Z

    ping @fabpot

    ---------------------------------------------------------------------------

    by Tobion at 2012-08-29T17:53:07Z

    @fabpot I think it's important to merge this before 2.1 final.