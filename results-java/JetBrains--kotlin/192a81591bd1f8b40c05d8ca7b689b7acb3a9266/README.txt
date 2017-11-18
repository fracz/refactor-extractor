commit 192a81591bd1f8b40c05d8ca7b689b7acb3a9266
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Wed Dec 26 17:00:48 2012 +0400

    Resolve qualified nested class expressions

    lookupNamespaceType now returns NamespaceType of a scope not only of the
    namespace found by name, but also of the classifier static classes scope found
    by the same name. This allows correct resolution of expressions
    "Class.Nested.member()", where Class comes from Java (previously it was
    resolved into a NamespaceDescriptor with a NamespaceType).

    NamespaceDescriptor.getNamespaceType() is deleted since there are no sense in
    namespace's NamespaceType alone anymore.

    Also some minor refactoring (referencedName param is useless)

     #KT-1174 In Progress