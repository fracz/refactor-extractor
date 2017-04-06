commit 4badf2503b61f2bf560302b242d862e6af5e4ec3
Author: Chris Beams <cbeams@vmware.com>
Date:   Wed Dec 12 13:33:21 2012 +0100

    Upgrade to AspectJ 1.7.1

    This change upgrades the spring-framework build to AspectJ 1.7.1 to
    ensure compatibility. We remain backward-compatible to at least AJ
    1.6.12 (the previous version), and likely back to 1.6.7.

    AJ 1.7 allows for weaving Java 7 bytecode, particularly with regard
    to invokedynamic instructions, and furthermore makes improvements to
    the aspectj language itself to allow users to take advantage of Java 7-
    style language features within aspects. See [1] for details.

    [1]: http://eclipse.org/aspectj/doc/released/README-170.html

    Issue: SPR-10079