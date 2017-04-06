commit 50ca944278c9d7bc6e0c69ab66ab0e9a993e426b
Author: Gabriel Ostrolucký <gadelat@gmail.com>
Date:   Sun Feb 19 23:03:20 2017 +0100

    [Serializer] Reduce complexity of NameConverter

    Cleaner and faster implementation of camelcase normalization.
    Speed improvement is particularly visible in long string input.