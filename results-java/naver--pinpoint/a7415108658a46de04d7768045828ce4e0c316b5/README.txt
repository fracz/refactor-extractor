commit a7415108658a46de04d7768045828ce4e0c316b5
Author: HyunGil Jeong <hyungil.jeong@navercorp.com>
Date:   Fri Jul 10 17:59:28 2015 +0900

    [#670] improve trace event ordering

    - SpanEvents now uses its wrapping Span's id when ordering
    - Asynchronous events are now guaranteed to be placed after its parent
    event (used to cause problems when async traces happened at the same
    time as its parent event)
    - Fix cases where nulls are passed into annotations when creating
    ExpectedTrace (cases where ExpectedAnnotation[] is used instead of
    varargs)