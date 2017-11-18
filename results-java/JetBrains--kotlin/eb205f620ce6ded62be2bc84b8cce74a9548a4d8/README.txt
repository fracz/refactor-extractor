commit eb205f620ce6ded62be2bc84b8cce74a9548a4d8
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Tue Jul 4 16:27:06 2017 +0300

    Do not use parameter descriptors in most annotation implementations

    Except AnnotationDescriptorImpl, which is refactored in the subsequent
    commit.

    Note that we no longer check the presence of parameters with the
    corresponding names in the annotation class in
    LazyJavaAnnotationDescriptor, this is why test data changed