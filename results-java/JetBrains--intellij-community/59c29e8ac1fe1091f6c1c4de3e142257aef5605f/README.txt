commit 59c29e8ac1fe1091f6c1c4de3e142257aef5605f
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Wed Sep 28 19:12:56 2016 +0300

    PY-18788 Remove redundant test as we first need to improve annotation parsing

    Specifically, we need to be able to parse annotations in text form and
    recognize those names that require additional import from "typing"
    module. Current implementation of PyTypeParser is not capable of that,
    since it omits types which names cannot be resolved in the context
    surrounding the type hint.

    This test was added preemptively, but it's going to take longer than
    expected to fix the problem, because we want to implement the
    aforementioned improvement in PyTypeParser as part of storing type
    annotations in stub files (see PY-18816).