commit 9eaf4f25be6216234f0a5dc5fd13b33f73f17d6c
Author: kstanger <kstanger@google.com>
Date:   Fri Mar 13 07:22:42 2015 -0700

    Moves JavaToIOSMethodTranslator above Functionizer because it needs to convert some ClassInstanceCreation nodes before Functionizer does.
    Small refactoring in JavaToIOSMethodTranslator.
            Change on 2015/03/13 by kstanger <kstanger@google.com>
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=88544183