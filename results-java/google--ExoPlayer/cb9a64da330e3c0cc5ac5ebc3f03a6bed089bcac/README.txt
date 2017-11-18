commit cb9a64da330e3c0cc5ac5ebc3f03a6bed089bcac
Author: olly <olly@google.com>
Date:   Sun Jun 5 06:23:41 2016 -0700

    Merge ID3 parsing improvements from GitHub.

    - Parse APIC and TextInformation frames.
    - In MPEG-TS, don't mind if packets contain end padding.
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=124079930