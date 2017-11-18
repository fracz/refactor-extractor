commit 0352bdbca568bbbdd9c44809e54c17443a7b4922
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Wed Jun 21 15:17:26 2017 +0300

    Optimize and improve AbstractClassTypeConstructor.equals

    Instead of computing and comparing FQ names, compare simple names of
    classes and theirs containers. This code was responsible for creation of
    about 10% of FqNameUnsafe instances during compilation of "core"
    modules.

    Also make the check more strict: previously, a class "c" declared in
    package "a.b" would be considered equal to a class "c" declared in class
    "b" in package "a". Because JVM type descriptors of such classes are
    different, this behavior was suspicious and might have lead to error at
    runtime. Now, we require the number of containing classes of the given
    two classes also to be the same