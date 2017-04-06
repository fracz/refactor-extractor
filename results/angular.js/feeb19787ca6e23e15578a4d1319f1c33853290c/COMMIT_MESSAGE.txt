commit feeb19787ca6e23e15578a4d1319f1c33853290c
Author: Martin Staffa <mjstaffa@googlemail.com>
Date:   Wed Jan 6 15:52:45 2016 +0100

    refactor(loader): move component definition code to the `$compileProvider`

    The `Module.component()` helper now delegates to `$compileProvider.component()`.

    This has the following benefits:

    - when using only the loader, we are not accessing out of scope variables / functions
    - components can be registered via $compileProvider
    - docs are a bit easier to find
    - it is easier to keep the Batarang version of the loader up to date if there is minimal
      code in that file.

    Closes #13692