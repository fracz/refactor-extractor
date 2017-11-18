commit 5aef20201c8229359941a09d15471c4819fcc8a6
Author: kstanger <kstanger@google.com>
Date:   Fri Mar 27 06:25:06 2015 -0700

    Removes postCollect() step from HeaderImportCollector and replaces it with improved logic in ObjectiveCHeaderGenerator that avoids forward declaring inner classes if their outer class is imported.
            Change on 2015/03/27 by kstanger <kstanger@google.com>
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=89689726