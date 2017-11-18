commit 9a2b54ca044d3e4a966c50a6933e7d08ac97f367
Author: Cedric Champeau <cedric@gradle.com>
Date:   Wed Feb 8 10:57:36 2017 +0100

    Be tolerant whenever a broken jar or class file is found on compile classpath

    Since the introduction of compile classpath snapshotting, and the improvements to annotation processor detection,
    we're much stricter with regards to what we find on classpath. If a jar is broken (unreadable, or has wrong entries)
    or that a class file is unreadable, we fail early and do not bother compiling. The justification for this is that
    we want to make sure compile classpath snapshotting is valid: if for example a bug is introduced in ASM, that we use
    for snapshotting, we want to make sure we realize this and not silently ignore errors and lead to wrong ABI signatures.

    But the consequence for the user is too big: upgrading from Gradle 3.3 to Gradle 3.4 can make compile fail just because
    of the presence of a broken Jar on classpath (even if it means, in practice, that the jar is probably a leaking dependency
    not used in sources, otherwise javac would also fail), or a broken class on classpath (even if it means that Javac itself
    would fail, so the class is unused). The reality is that there are many broken jars in the wild, apparently. So we
    start with being more lenient, and tolerate them, but warn the user of the presence of a broken jar/class, and that we
    will not support this in Gradle 5.0. This gives the opportunity to the user to either fix the jar, remove the leaking
    dependency, or ask to the maintainers of the dependency to fix it.