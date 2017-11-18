commit efa4bf1d74a14b34a4e56bc2289d6096a6147a34
Author: Pavel V. Talanov <talanov.pavel@gmail.com>
Date:   Tue Nov 17 15:09:01 2015 +0300

    Script refactoring, frontend: Treat scripts as classes (as opposed to function bodies)

    ScriptDescriptor implements ClassDescriptor
    Scripts are accessible in IDE via stub index
    Replace special treatment of scripts with generic mechanism in several cases
    Scripts to longer generate 'rv' property for storing result value (changes in repl required)