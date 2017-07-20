commit a3b6dfbb7b0588f53afcb59b6b338aa36d9715ec
Author: Carsten Brandt <mail@cebe.cc>
Date:   Fri Dec 16 02:20:02 2016 +0100

    Catch `\Throwable` in critical places

    Added catch `\Throwable` to be compatible with PHP7.
    Added it in cases where object state needs to be kept consistent.

    Mainly on transactions but also some other places where some values are
    reset before exiting.

    Most of them could probably be refactored by using `finally` in 2.1, as
    that requires PHP 5.5.

    fixes #12619