commit b03b0f22d1e54733c2d39c7f913f971dab14ebfa
Author: Nikolay Fedorovskikh <fenik17@gmail.com>
Date:   Wed Jun 7 01:41:33 2017 +0500

    Removing a SeekAheadNoBackArrayException to avoid exception handling

    Motivation:

    A `SeekAheadNoBackArrayException` used as check for `ByteBuf#hasArray`. The catch of exceptions carries a large overhead on stack trace filling, and this should be avoided.

    Modifications:

    - Remove the class `SeekAheadNoBackArrayException` and replace its usage with `if` statements.
    - Use methods from `ObjectUtils` for better readability.
    - Make private methods static where it make sense.
    - Remove unused private methods.

    Result:

    Less of exception handling logic, better performance.