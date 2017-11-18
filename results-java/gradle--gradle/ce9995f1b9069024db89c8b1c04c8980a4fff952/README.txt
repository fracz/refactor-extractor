commit ce9995f1b9069024db89c8b1c04c8980a4fff952
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Dec 18 16:45:59 2012 +0100

    Improved the consistency and readability of printing the modules / dependencies.

    Instead of: "group: org.gradle, name: someLib, version: 1.0"
    It is now: "org.gradle:someLib:1.0"

    The change is will have visual impact in quite a few places, mostly at resolution failures. Also, toString() implementations of ceratin core types (ModuleVersionIdentifier, ModuleVersionSelector) are updated accordingly. This way, the reports should be more readable and more consistent (we already use the standard gav notation in many places).