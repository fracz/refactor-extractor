commit 3f96fa8ce9741e500a7bd2435741d37eb5b9ecbf
Author: Tagir Valeev <Tagir.Valeev@jetbrains.com>
Date:   Wed May 17 11:04:39 2017 +0700

    KeyFMap improvements (IDEA-CR-21117)

    1. JavaDoc
    2. toString() unified
    3. equalsByReference, identityHashCode, size
    4. implementations are package-private, getters removed
    5. plus() can return self if new value is the same as existing one