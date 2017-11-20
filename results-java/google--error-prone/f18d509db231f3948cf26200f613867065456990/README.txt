commit f18d509db231f3948cf26200f613867065456990
Author: eaftan <eaftan@google.com>
Date:   Fri May 27 14:40:00 2016 -0700

    Disable checks that are more stylistic than functional

    This will improve signal-to-noise ratio for Android and our external
    users. Specifically, disables ClassName, DepAnn,
    LongLiteralLowerCaseSuffix, MultipleTopLevelClasses, and
    StaticAccessedFromInstance.

    MOE_MIGRATED_REVID=123459006