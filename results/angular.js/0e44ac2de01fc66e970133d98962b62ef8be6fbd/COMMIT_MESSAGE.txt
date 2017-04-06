commit 0e44ac2de01fc66e970133d98962b62ef8be6fbd
Author: Igor Minar <igor@angularjs.org>
Date:   Tue Aug 26 13:40:44 2014 -0700

    refactor(hashKey): don't generate memory garbage

    we now store both the object type and the id as the hashkey and return it for all objects.

    for primitives we still have to do string concatination because we can't use expandos on them to
    store the hashkey