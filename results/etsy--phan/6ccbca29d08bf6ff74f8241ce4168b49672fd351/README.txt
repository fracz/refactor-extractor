commit 6ccbca29d08bf6ff74f8241ce4168b49672fd351
Author: Tyson Andre <tysonandre775@hotmail.com>
Date:   Sat Jul 15 21:21:27 2017 -0700

    Expand checks to those which unconditionally continue/break

    (e.g. improve analysis of `if (!is_string($x)) { break; }`)

    - This should be fine, since for loops already need to preserve the old
      types of variables.
    - In the future, more comprehensive checks would merge the Context at
      every single point with a break/continue.

    Also, fix failing tests to reflect `iterable` being returned in the expanded
    union types of `Traversable`.