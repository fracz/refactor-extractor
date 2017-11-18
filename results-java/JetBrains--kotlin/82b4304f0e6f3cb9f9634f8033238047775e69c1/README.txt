commit 82b4304f0e6f3cb9f9634f8033238047775e69c1
Author: Evgeny Gerashchenko <Evgeny.Gerashchenko@jetbrains.com>
Date:   Sat Apr 21 02:39:23 2012 +0400

    Added auto-importing in JetChangePropertyActions.addTypeAnnotation(). It is used in "specify type explicitly" intention and "introduce variable" refactoring. Corrected auto-importing for cases of nested classes (e.g. Map.Entry).