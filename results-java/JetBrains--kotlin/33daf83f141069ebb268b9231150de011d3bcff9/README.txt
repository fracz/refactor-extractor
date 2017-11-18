commit 33daf83f141069ebb268b9231150de011d3bcff9
Author: Alexey Andreev <Alexey.Andreev@jetbrains.com>
Date:   Thu Jun 16 15:15:29 2016 +0300

    KT-2752: refactoring:
    1. Get rid of most of ManglingUtils
    2. Use simple mangling for delegated properties instead of stable mangling
    3. Use stable mangling for public declarations of open non-public classes
    4. When generating a fresh name in a JsScope, check it for clashing against parent scopes
    5. JsFunctionScope does not generate fresh name instead of stable names
    6. Function scopes inherit directly from global scope
    7. Generate simple mangled names for backing fields of properties