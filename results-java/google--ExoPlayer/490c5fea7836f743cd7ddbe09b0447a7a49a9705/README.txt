commit 490c5fea7836f743cd7ddbe09b0447a7a49a9705
Author: olly <olly@google.com>
Date:   Tue Aug 2 05:30:12 2016 -0700

    Further improve codec reconfiguration

    - Only setup a codec to allow adaptation to other compatible
      formats in the stream. If something like the mimeType is
      changing adaptation will never be possible, so there's no
      point.
    - Incorporate maxInputSize into the reconfiguration logic.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=129088464