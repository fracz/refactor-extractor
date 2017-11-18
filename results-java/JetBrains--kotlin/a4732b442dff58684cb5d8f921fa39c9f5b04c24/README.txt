commit a4732b442dff58684cb5d8f921fa39c9f5b04c24
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Tue Oct 6 21:55:49 2015 +0300

    Don't create KClass and KPackage instances in <clinit>

    This proved to be a fragile technique, which probably doesn't even improve
    performance in most cases but has lots of unexpected problems: unconditional
    initialization of reflection classes, increasing the size of the bytecode, bugs
    with <clinit> in annotations on JVM 6, inability to support conversion of a
    class from Kotlin to Java without recompiling clients which use it
    reflectively, etc.