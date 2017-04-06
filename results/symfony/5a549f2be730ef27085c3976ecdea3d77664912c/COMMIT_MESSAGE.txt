commit 5a549f2be730ef27085c3976ecdea3d77664912c
Merge: d3361c1 e81c710
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Oct 23 09:50:26 2011 +0200

    merged branch stof/profiler_priority (PR #2439)

    Commits
    -------

    e81c710 Increased the priority of the profiler request listener

    Discussion
    ----------

    Increased the priority of the profiler request listener

    If a request listener returns a response before calling the profiler
    listener, the request will not be added in the stack leading to an error
    during the handling of the kernel.response event. The profiler listener
    should ideally be run first.

    The same issue will occur if a previous listener throws an exception btw.

    ---------------------------------------------------------------------------

    by schmittjoh at 2011/10/20 08:21:15 -0700

    Can you add a test?

    These interdependencies between listeners are really easy to mess up, and no-one will remember why listener X has to be run before listener Y, but after listener Z.

    ---------------------------------------------------------------------------

    by stof at 2011/10/20 08:27:51 -0700

    Well, in fact, the only rule is that the listener **must** run for the request event to avoid failing during the response event of an eventual subrequest. So if another listener triggers a subrequest before the profiler is called, it will fail because the parent request is not in the stack.

    A better fix would probably make the listener work even when the propagation of the request event has been stopped (or has not reached this listener yet) to avoid issues (someone doing a subrequest in its own listener will still be able to use a higher priority for instance) but I don't have time to refactor @fabpot's refactoring now.

    ---------------------------------------------------------------------------

    by schmittjoh at 2011/10/20 08:36:40 -0700

    Another idea would be to add a ProfilerHttpKernel as it basically needs to wrap the request/response cycle, and people might have priorities beyond 1000 (see @Seldaek's SecurityBundle).

    Using an extra kernel would be the only way to really guarantee that it is working. Note that I'm not implying you should do it, but as a "real" fix this would probably be better.

    ---------------------------------------------------------------------------

    by stof at 2011/10/20 08:43:07 -0700

    The issue by wrapping the HttpKernel is that the kernel passed in the event is the one dispatching the event. So we would have to implement it by using inheritance, not composition (otherwise the listener would create subrequests using the inner kernel and they would not be profiled).
    But FrameworkBundle already extend the HttpKernel to handle the creation of the request scope (which must be done using inheritance for the same reason).
    So it would require having a ContainerAwareHttpKernel (handling the scope), a ProfilerHttpKernel (handling the profiler) both extending the base HttpKernel (the Profiler one should be usable when using only the component, and the ContainerAware one is needed in prod when disabling the profiler) but also a ContainerAwareProfilerHttpKernel extending one of them and duplicating the logic of the other.

    ---------------------------------------------------------------------------

    by schmittjoh at 2011/10/20 08:52:13 -0700

    So maybe, we need to revisit which kernel is passed in the event as well.

    But doing things more explicitly is better than making assumptions about priorities that no-one can remember in a few weeks.

    ---------------------------------------------------------------------------

    by stof at 2011/10/20 09:53:45 -0700

    @schmittjoh Issue is, using composition would require to let the inner kernel know about the wrapper to be able to pass it in the event. This looks ugly.

    But I agree it would be better to do things explicitly

    ---------------------------------------------------------------------------

    by Seldaek at 2011/10/21 07:39:23 -0700

    Didn't read everything, busy, but regarding the mention of our security bundle, it is highlighting a big issue with priorities imo, in that we're missing a one-stop page that lists all the existing priorities, and possibly some more details like "if you do this, don't go above this priority or it'll blow up". Otherwise it'll just be a game of escalation :)