commit c1d40f1e1558c48624a5c2f336f1bc0e97eb9f8b
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Wed Jul 31 17:33:46 2013 +0400

    Introduce JavaClassifier

    JavaClassifier is a common type for JavaClass and JavaTypeParameter. In almost
    all of the places JavaClass remains to be JavaClass, except for the resolution
    result of JavaClassType, which can be a type parameter. Add an assertion that
    JavaClass is created only for a class and not for a type parameter. Delete
    different creations of JavaClass in 'idea' (where there can be a type
    parameter) by changing the interface of util functions.

    Delete JavaModifierListOwnerImpl, extract its methods into JavaElementUtil and
    use these implementations for every JavaModifierListOwner

    Continue refactoring JavaTypeTransformer to work with JavaType, not PsiType