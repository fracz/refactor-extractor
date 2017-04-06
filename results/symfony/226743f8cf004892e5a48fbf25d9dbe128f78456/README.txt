commit 226743f8cf004892e5a48fbf25d9dbe128f78456
Merge: 9660530 56fe8d1
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Nov 12 15:15:24 2012 +0100

    merged branch fabpot/code-helpers (PR #5986)

    This PR was merged into the master branch.

    Commits
    -------

    56fe8d1 duplicated the code helper code to the Twig bundle

    Discussion
    ----------

    moved code helper code to the Twig bundle

    These helpers are very specific and are only used in TwigBundle for the
    profiler and the exception templates.

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-11-12T09:12:56Z

    Is there a reason for this BC break other than a cosmetical tweak?

    If not strictly necessary, I'd like to see these kind of changes being scheduled for 3.0.

    ---------------------------------------------------------------------------

    by fabpot at 2012-11-12T09:29:35Z

    Of course, I don't want to make this change without a reason and indeed, I forgot to mention the why of this change. Let me explain.

    I've been working on the integration of the Symfony web profiler into other OSS that use HttpKernel like Silex and Drupal for quite some time now. That's why I developed the new namespace feature in Twig, that's why I've refactored the web profiler to only use Twig and not the templating component. One of the last step is this PR, which reduces the number of dependencies to be able to use the WebProfiler bundle.

    So, this change is not cosmetic, but one more step towards the goal of making the web profiler more reusable.

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-11-12T13:22:28Z

    I see, makes sense. How about duplicating the code for now?

    After looking through the history, it doesn't seem like it changes often
    (the last real code change was a year ago). So, it should not be much more
    maintenance effort, and we could keep BC here.

    On Mon, Nov 12, 2012 at 10:29 AM, Fabien Potencier <notifications@github.com
    > wrote:

    > Of course, I don't want to make this change without a reason and indeed, I
    > forgot to mention the why of this change. Let me explain.
    >
    > I've been working on the integration of the Symfony web profiler into
    > other OSS that use HttpKernel like Silex and Drupal for quite some time
    > now. That's why I developed the new namespace feature in Twig, that's why
    > I've refactored the web profiler to only use Twig and not the templating
    > component. One of the last step is this PR, which reduces the number of
    > dependencies to be able to use the WebProfiler bundle.
    >
    > So, this change is not cosmetic, but one more step towards the goal of
    > making the web profiler more reusable.
    >
    > —
    > Reply to this email directly or view it on GitHub<https://github.com/symfony/symfony/pull/5986#issuecomment-10281201>.
    >
    >

    ---------------------------------------------------------------------------

    by fabpot at 2012-11-12T13:32:33Z

    Of course, that's a possibility. But what for? I doubt that people are using this code. Are you using this code somewhere?

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-11-12T13:37:24Z

    Yes, I believe that I'm using it both in JMSDebuggingBundle, and
    JMSJobQueueBundle as both are rendering exception stack traces at some
    point.

    On Mon, Nov 12, 2012 at 2:32 PM, Fabien Potencier
    <notifications@github.com>wrote:

    > Of course, that's a possibility. But what for? I doubt that people are
    > using this code. Are you using this code somewhere?
    >
    > —
    > Reply to this email directly or view it on GitHub<https://github.com/symfony/symfony/pull/5986#issuecomment-10287353>.
    >
    >

    ---------------------------------------------------------------------------

    by fabpot at 2012-11-12T14:11:50Z

    ok, fair enough. The code is now in the framework bundle as well.