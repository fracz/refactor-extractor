commit 26342f6fe1f8b629a2ce1bdb3ef872f84a7e960a
Author: cushon <cushon@google.com>
Date:   Wed Jul 22 17:01:33 2015 -0700

    Work around a type inference change in javac

    The javac compiler's behavior when handling wildcards and "capture" type
    variables has been improved for conformance to the language specification. This
    improves type checking behavior in certain unusual circumstances. It is also a
    source-incompatible change: certain uses of wildcards that have compiled in the
    past may fail to compile because of a program's reliance on the javac bug.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=98889343