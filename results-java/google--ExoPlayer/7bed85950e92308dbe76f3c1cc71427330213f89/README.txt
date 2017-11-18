commit 7bed85950e92308dbe76f3c1cc71427330213f89
Author: olly <olly@google.com>
Date:   Thu Jul 14 09:46:29 2016 +0100

    Fix/improve SmoothStreaming live window

    - Add missing callback call.
    - Allow injection of live edge offset.
    - Refine calculation of live window size to correctly
      handle just-started streams where the DVR window
      hasn't yet grown to full size.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=130412465