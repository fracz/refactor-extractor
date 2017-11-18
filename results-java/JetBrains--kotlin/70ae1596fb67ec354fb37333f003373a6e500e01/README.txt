commit 70ae1596fb67ec354fb37333f003373a6e500e01
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Wed Sep 6 15:14:47 2017 +0300

    Support JvmPackageName annotation in binary format

    The main changes are in jvm_package_table.proto and ModuleMapping.kt.
    With JvmPackageName, package parts can now have a JVM package name that
    differs from their Kotlin name. So, in addition to the old package parts
    which were stored as short names + short name of multifile facade (we
    can't change this because of compatibility with old compilers), we now
    store separately those package parts, which have a different JVM package
    name. The format is optimized to avoid storing any package name more
    than once as a string.

    Another notable change is in KotlinCliJavaFileManagerImpl, where we now
    load .kotlin_module files when determining whether or not a package
    exists. Before this change, no PsiPackage (and thus, no JavaPackage and
    eventually, no LazyJavaPackageFragment) was created unless there was at
    least one file in the corresponding directory. Now we also create
    packages if they are "mapped" to other JVM packages, i.e. if all package
    parts in them have been annotated with JvmPackageName.

    Most of the other changes are refactorings to allow internal names of
    package parts/multifile classes where previously there were only short
    names.