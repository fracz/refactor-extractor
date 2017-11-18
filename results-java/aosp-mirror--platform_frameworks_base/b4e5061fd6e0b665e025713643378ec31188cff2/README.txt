commit b4e5061fd6e0b665e025713643378ec31188cff2
Author: Mathieu Chartier <mathieuc@google.com>
Date:   Wed Sep 10 12:56:21 2014 -0700

    Change EmptyArray System.identityHashCode to Object.hashCode.

    Equivalent behavior, improves performance since
    Object.hashCode has a fast path in the java side that does not
    require JNI.

    According to traceview sampling profiler:
    Calendar had 6.8% time in System.identityHashCode during launch.
    0.4% time in System.identityHashCode after the change.

    Bug: 16828525

    Change-Id: I1ed1d1283a990f990b0d4352cc1f4822b1dadf7b