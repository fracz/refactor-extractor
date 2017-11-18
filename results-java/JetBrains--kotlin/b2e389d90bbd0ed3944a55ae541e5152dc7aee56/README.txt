commit b2e389d90bbd0ed3944a55ae541e5152dc7aee56
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Tue Apr 9 15:47:53 2013 +0400

    Minor refactoring in FunctionCodegen

    Combine two nested classes, MethodBounds and LocalVariablesInfo, into one
    MethodInfo. Rename some methods. No logic has changed