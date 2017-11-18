commit 72f37a278cea091f958aa5cd28a2edc38e665a3a
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Wed Jun 28 15:19:14 2017 +0300

    Do not add all modules from module path when compiling unnamed module

    Note that javac reports a nice error in this case ("package foo is
    declared in module lib, which is not in the module graph"), but we only
    report "unresolved reference" because the corresponding modules are not
    added to classpath roots. We should improve this in the future

     #KT-18598 In Progress