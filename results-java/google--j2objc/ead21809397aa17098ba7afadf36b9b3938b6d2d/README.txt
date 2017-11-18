commit ead21809397aa17098ba7afadf36b9b3938b6d2d
Author: kstanger <kstanger@google.com>
Date:   Thu Aug 28 06:05:03 2014 -0700

    Moves IntegralToString implementations to native code to improve performance and eliminate dependency on ThreadLocal.
            Change on 2014/08/28 by kstanger <kstanger@google.com>
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=74303249