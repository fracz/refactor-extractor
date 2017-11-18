commit 7d08b4fc357e12ee2487e87d8fdcbeee1152e5a0
Author: Norman Maurer <norman_maurer@apple.com>
Date:   Tue Feb 21 20:33:11 2017 +0100

    Remove optional dependency on javassist

    Motivation:

    We shipped a javassist based implementation for typematching and logged a confusing debug message about missing javassist. We never were able to prove it really gives any perf improvements so we should just remove it.

    Modifications:

    - Remove javassist dependency and impl
    - Fix possible classloader deadlock as reported by intellij

    Result:

    Less code to maintain and less confusing log message.