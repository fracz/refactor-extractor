commit 88353575e483329eeb958a016cec8773be8e328c
Merge: 57990cc 77185e0
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Apr 11 08:28:45 2012 +0200

    merged branch vicb/routing_dumpers (PR #3858)

    Commits
    -------

    77185e0 [Routing] Allow spaces in the script name for the apache dumper
    6465a69 [Routing] Fixes to handle spaces in route pattern

    Discussion
    ----------

    [Routing] Handling of space characters in the dumpers

    The compiler was using the 'x' modifier in order to ignore extra spaces and line feeds but the code was flawed:

    - it was actually ignoring all the spaces, not only the extra ones added by the compiler,
    - all the spaces were stripped in the php and apache matchers.

    The proposed fix:

    - do not use the 'x' modifier any more (and then do no add extra spaces / line feeds),
    - do not strip the spaces in the matchers,
    - escapes the spaces (both in regexs and script name) for the apache matcher.

    It also include [a small optimization](https://github.com/vicb/symfony/pull/new#L9L89) when the only token of a route is an optional variable token - the idea is to make the regex easier to read.

    ---------------------------------------------------------------------------

    by vicb at 2012-04-10T13:59:45Z

    @Baachi fixed now. Thanks.

    ---------------------------------------------------------------------------

    by Tobion at 2012-04-10T16:01:31Z

    +1, I saw no reason for pretty printing the regex in the first place (just for debugging I guess).
    @vicb since you want to make the regex easier to read, I propose the remove the `P` from the variable regex `?P<bar>`, which is not needed anymore in PHP 5.3 (and we only support PHP 5.3+ anyway).

    ---------------------------------------------------------------------------

    by vicb at 2012-04-10T16:08:36Z

    @Tobion could you make a PR to this branch for the named parameters ?

    ---------------------------------------------------------------------------

    by Tobion at 2012-04-10T16:12:34Z

    I can include it in #3754 because I'm about the add 2 more fixes to it anyway.
    But when I proposed to apply these fixes to 2.0 Fabien rejected it. So not sure what branch you want me to apply this.

    ---------------------------------------------------------------------------

    by vicb at 2012-04-10T16:25:38Z

    May be the best is to put it on hold while I am reviewing your PRs. There are already enough changes, we'll make an other PR after all have been sorted out.

    What's the difference between 3754 and 3810 ? (3810 + 3763 = 3754 ?)

    ---------------------------------------------------------------------------

    by Tobion at 2012-04-10T16:39:32Z

    Lol you forget to link the PR numbers. At first sight I thought it's some sort of mathematical riddle. Haha
    #3810 is for 2.0 =  #3763 (already merged) + #3754 for master

    ---------------------------------------------------------------------------

    by vicb at 2012-04-10T16:52:18Z

    I didn't link on purpose... the question is if '=' means strictly or loosely equal (any diffs - beside master vs 2.0) ?

    ---------------------------------------------------------------------------

    by Tobion at 2012-04-10T17:06:04Z

    It just applies my changes to 2.0. Nothing more. So master still differs from 2.0 by the addional features that were already implemented (e.g. `RouteCollection->addCollection` with optional requirements and options). But since my changes are bug fixes (except the performance improvement in #3763 but that doesn't break anything and makes 2.0 easier to maintain) I thought they should go into 2.0 as well.

    ---------------------------------------------------------------------------

    by vicb at 2012-04-10T17:14:27Z

    @Tobion only bug fixes mean "only bug fixes". You should re-open a PR for 2.0 with "only bug fixes", you might want to wait for me to review 3754.

    ---------------------------------------------------------------------------

    by Tobion at 2012-04-10T17:21:00Z

    Without #3763 it's much harder to apply the bug fixes. And now that I found 2 more bugs which requiresome rewriting of the PhpMatcherDumper, I don't want to apply all the commits by hand again for 2.0...