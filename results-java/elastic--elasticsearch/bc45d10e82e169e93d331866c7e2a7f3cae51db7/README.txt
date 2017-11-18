commit bc45d10e82e169e93d331866c7e2a7f3cae51db7
Author: Nik Everett <nik9000@gmail.com>
Date:   Wed Apr 26 16:04:38 2017 -0400

    Remove most usages of 1-arg Script ctor (#24325)

    The one argument ctor for `Script` creates a script with the
    default language but most usages of are for testing and either
    don't care about the language or are for use with
    `MockScriptEngine`. This replaces most usages of the one argument
    ctor on `Script` with calls to `ESTestCase#mockScript` to make
    it clear that the tests don't need the default scripting language.

    I've also factored out some copy and pasted script generation
    code into a single place. I would have had to change that code
    to use `mockScript` anyway, so it was easier to perform the
    refactor.

    Relates to #16314