commit 1036506b25d0ce0dcbcb90206e38a26fd1d6b409
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Mon Sep 21 20:35:27 2015 +0300

    Introduce new string table optimized for JVM class files

    This format of the string table allows to reduce the size of the Kotlin
    metadata in JVM class files by reusing constants already present in the
    constant pool. Currently the string table produced by JvmStringTable is not
    fully optimized in serialization (in particular, the 'substring' operation
    which will be used to extract type names out of generic signature, is not used
    at all), but the format and its complete support in the deserialization
    (JvmNameResolver) allows future improvement without changing the binary version