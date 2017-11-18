commit bd0f97bb63ab9218e9b809a2f0713a21fdeac149
Author: Daniil Ovchinnikov <daniil.ovchinnikov@jetbrains.com>
Date:   Thu Nov 13 19:03:20 2014 +0300

    IDEA-126120 Groovy: @CompileStatic improvements.
    - void methods return type is void in static compile mode
    - DGM methods are less preferable than others
    - type autoboxing removed
    - fields & methods in compile static context have declared type instead of inferred