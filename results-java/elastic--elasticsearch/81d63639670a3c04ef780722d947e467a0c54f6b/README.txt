commit 81d63639670a3c04ef780722d947e467a0c54f6b
Author: Ryan Ernst <ryan@iernst.net>
Date:   Thu Dec 10 19:29:54 2015 -0800

    Remove benchmark package

    Tons of ancient "benchmarks" exist in elasticsearch. These are main
    methods that do some kind of construction of ES classes and time various
    things. The problem with these is they are not maintained, and not run.
    Refactorings that touch anything that is common in these classes is very
    painful. Going through these, almost all would simply not work in 2.x
    without modifications (because they do not set path.home).

    This change removes the entire benchmark package. If someone needs to
    run a benchmark like this, they can look at history for examples if
    necessary (although these examples are often not realistic and should
    just start real elasticsearch processes in a shell script). Longer term,
    we should make this easier to do by having the build support adding real
    benchmarks which can be run in jenkins (so we know they actually run,
    instead of doing refactorings with pure guesswork as to whether the
    benchmark would run correctly).