commit 14a1fffd5e94a8835cb7d39c1d6fc5a202c501da
Author: cushon <cushon@google.com>
Date:   Sun Jul 19 11:05:36 2015 -0700

    Work around a type inference change in javac

    The javac compiler's behavior when handling wildcards and "capture" type
    variables has been improved for conformance to the language specification. This
    improves type checking behavior in certain unusual circumstances. It is also a
    source-incompatible change: certain uses of wildcards that have compiled in the
    past may fail to compile because of a program's reliance on the javac bug.

    RELNOTES: N/A
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=99196709