commit 9055ff26ebccd457ae5d2492e4fa8d4ab2f67664
Author: cparsons <cparsons@google.com>
Date:   Mon Sep 18 23:52:04 2017 +0200

    use toList.contains for depset containsKey instead of toSet

    This should be a slight performance improvement on the previous implementation, as NestedSet.toSet() calls toList() and then throws the contents into a set

    RELNOTES: None.
    PiperOrigin-RevId: 169150743