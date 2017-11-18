commit bf906433e1838bdec596cee6f3d76e853ba7b594
Author: Cedric Champeau <cedric@gradle.com>
Date:   Mon May 2 11:10:15 2016 +0200

    Replace usage of `String#format` with good old string concatenation

    The rationale behind this change is that while `String#format` is usually admitted as good for readability (this is arguable),
    in practice it is very bad for performance. Since Java 6, the JVM (the JIT, to be correct) does a pretty good job at optimizing
    string concatenation. However, it is only capable to do so if we're using "dirty" string concatenations (using `+`), or `StringBuilder`.

    However, usage of `StringBuilder` is not recommended either for 2 reasons:

    1. it significantly decreases code readability
    2. in Java 9, String concatenation is even more optimized thanks to `invokedynamic`, and code using `StringBuilder` will *not* benefit
    from this optimization.

    There are sill leftover `String#format` calls in the code, in the following cases:

    - the call is used to generate an exception message. That's 90% of the remaining calls.
    - the call is obviously a debug message, often used in `toString` (because as we all know, `toString` should only be used for debugging)
    - the format includes specific number formatting patterns
    - I missed it