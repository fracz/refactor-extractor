commit 77f5174f0d23428fced1e6fd25b2556803ce9079
Author: Wenlei Xie <wenlei.xie@gmail.com>
Date:   Sat Aug 12 14:47:59 2017 -0700

    Add ArgumentProperty to describe scalar function argument

    Scalar function arguments have the following three properties:
    * nullable
    * hasNullFlag
    * lambdaInterface

    This makes code difficult to understand and maintain.

    This commit refactors them into ArgumentProperty:
    * For value type argument, NullConvention will be presented,
    * For function type argument, the lambdaInterface will be presented.