commit 9eca5367bda1a89473b8beb3b121848822895f7b
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Mon Jun 5 19:06:17 2017 +0300

    PY-8174 Don't change the original call in change signature

    It doesn't make much sense to modifying the call expression as
    the result of refactoring which sole purpose was to match the definition
    and the rest of the usages with this very expression.