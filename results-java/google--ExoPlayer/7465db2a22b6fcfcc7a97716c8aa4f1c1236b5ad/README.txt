commit 7465db2a22b6fcfcc7a97716c8aa4f1c1236b5ad
Author: olly <olly@google.com>
Date:   Thu May 19 10:16:44 2016 -0700

    Merge ID3 parsing improvements from GitHub.

    - Parse APIC and TextInformation frames.
    - In MPEG-TS, don't mind if packets contain end padding.
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=122743786