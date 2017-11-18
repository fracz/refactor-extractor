commit bc101b9841a898d7908407ea52dd623c0e3ee8e8
Author: Eddie Aftandilian <eaftan@google.com>
Date:   Fri May 27 21:40:00 2016 +0000

    For open-source Error Prone, disable checks that are more stylistic than
    functional.  This will improve signal-to-noise ratio for Android and our
    external users.

    Specifically, disables ClassName, DepAnn, LongLiteralLowerCaseSuffix,
    MultipleTopLevelClasses, and StaticAccessedFromInstance.

    --
    MOS_MIGRATED_REVID=123459006