commit dd9046985e21e2b34e229a0eda326bf403607232
Author: Adrian Cole <acole@pivotal.io>
Date:   Tue Mar 29 16:42:01 2016 +0800

    Makes AsyncSpanConsumer the authoritative type for receiving spans

    Before, `accept(List<Span>)` was on `SpanStore`, a blocking interface.
    This helped for a while, but it is clear storage commands should be
    async, particularly for reasons listed in #133.

    This change removes writes from `SpanStore` and re-organizes the code
    around `AsyncSpanConsumer`. This is the first refactor on the way
    towards a component model #135.