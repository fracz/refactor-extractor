commit 24c533df325cbf684cde51fa1c9653f8fd42a300
Author: Evgeny Gerashchenko <Evgeny.Gerashchenko@jetbrains.com>
Date:   Thu Apr 23 18:13:44 2015 +0300

    KT-6285 Rename refactoring doesn't rename named arguments when rename function parameters or private val/var constructor parameters

    Just added test. Works automatically since rename parameter now delegates to change signature.

     #KT-6285 fixed