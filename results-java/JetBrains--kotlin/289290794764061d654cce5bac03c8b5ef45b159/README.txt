commit 289290794764061d654cce5bac03c8b5ef45b159
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Mon Dec 9 23:05:49 2013 +0400

    Delete Enum.name()/ordinal() JVM intrinsics

    Bytecode is the same if they're generated as is from descriptors

    + Some minor code style improvements