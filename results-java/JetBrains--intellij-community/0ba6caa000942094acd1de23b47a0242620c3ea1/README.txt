commit 0ba6caa000942094acd1de23b47a0242620c3ea1
Author: Pavel Dolgov <pavel.dolgov@jetbrains.com>
Date:   Thu Dec 1 13:24:15 2016 +0300

    Java: Check if 'java.base' is explicitly required in the inspection "Redundant 'requires' statement in module-info". Minor refactoring of RefJavaModule, to make the inspection code more straightforward. (IDEA-163139)