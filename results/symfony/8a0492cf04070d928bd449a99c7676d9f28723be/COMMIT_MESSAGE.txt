commit 8a0492cf04070d928bd449a99c7676d9f28723be
Merge: 2d2b9bc 5377290
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Feb 4 13:46:32 2013 +0100

    merged branch dlsniper/small-di-improvement (PR #6725)

    This PR was submitted for the master branch but it was merged into the 2.2 branch instead (closes #6725).

    Commits
    -------

    957b3af [DependencyInjection]Micro-optimization

    Discussion
    ----------

    [DependencyInjection]Micro-optimization

    Bug fix: no
    Feature addition: not really
    Backwards compatibility break: no
    Fixes the following tickets: ~
    Todo: see below
    License of the code: MIT
    Documentation PR: ~

    This adds a small micro-optimization to the container once it's dumped as PHP.

    Even if the speed bump isn't that visible on the default Symfony2 Standard distribution, this doesn't break anything and it should be helpful when having a lot of parameters in the container.

    I've did got better results with this applied but it would be helpful if others could confirm this to be true as well.

    ---------------------------------------------------------------------------

    by fabpot at 2013-01-13T19:57:36Z

    I'm -1 on this change. I really doubt that this has any significant perf improvement and it makes the code less clear. In fact, we rejected similar changes in the past.

    ---------------------------------------------------------------------------

    by alexandresalome at 2013-01-13T20:32:54Z

    This code is executed on compilation time, which means it's not executed in each request of production server.

    ---------------------------------------------------------------------------

    by igorw at 2013-01-13T21:03:33Z

    @alexandresalome no, this is changing the generated code that will be run on every request ;-). But I agree that it's probably not worth it. Unless there is a benchmark proving *significant* gains.

    ---------------------------------------------------------------------------

    by dlsniper at 2013-01-13T21:05:52Z

    @alexandresalome the code is execute at runtime since this changes the dumper part of it.
    @fabpot I didn't knew about the changes like this but I from my recent experience with optimizing various applications this kind of change was always improving speed. Also, aside from the fact that this is a known trick, readability was taken into consideration, that's why I've added the change only in the dumper.

    I've did a benchmark against a default container from Symfony2 and you can get it from here: https://gist.github.com/4526170

    Feel free to close this if you want.

    Note: Currently I'm trying to find and improve all the things related to speed in Symfony2, see the ticket here: symfony/symfony-standard#464 which started from this: https://groups.google.com/d/msg/symfony2/-gvguJqox3s/bjaA6Ks3EZoJ which was confirmed by the OP as well. This won't bring big performance increases, but it's a step forward imho.

    Thanks!

    ---------------------------------------------------------------------------

    by vicb at 2013-01-14T07:21:12Z

    @dlsniper there seems to be a huge diff in your bench between isset and ake, any chance you have xdebug turned on ?

    ---------------------------------------------------------------------------

    by dlsniper at 2013-01-14T08:03:35Z

    @vicb no, both xhprof / xdebug where turned off. Also keep in mind that there's a ` E-5 ` so the performance impact is not that much in terms of microseconds, that's why I've said micro-optimization, but it is still there and I've had projects with more that 1.3k values in that array and the project was medium in size.

    That's why I've said, someone else please test it as well and provided the benchmark, to prove that the ` isset + array_key_exists ` is faster that ` array_key_exists ` when the searched value is not null.

    Also, the benchmark assumes that there are only 300 searches in the parameters array which might be more or less that usual numbers and it only retrieves the same parameter every time.

    ---------------------------------------------------------------------------

    by vicb at 2013-01-14T08:08:57Z

    @dlsniper I am not referring to absolute values but to the ratio that is high.

    ---------------------------------------------------------------------------

    by mvrhov at 2013-01-14T08:29:12Z

    isset is known to be a way faster than ake. BTW my work project which I consider relatively small has just shy of 1000 entries in parameters array

    ---------------------------------------------------------------------------

    by vicb at 2013-01-14T09:29:59Z

    @mvrhov you're right, isset is faster but [especially when xdebug is on](http://ilia.ws/archives/247-Performance-Analysis-of-isset-vs-array_key_exists.html) - 2.5x vs 10x.

    I would generally :-1: micro-optims but this one is in a generated file so :+1: