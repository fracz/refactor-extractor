commit d62522982f39f2610f0d9d26d9908ccb1ea24222
Author: Sang Gi Ryu <ryucsion@gmail.com>
Date:   Thu Oct 23 13:27:50 2014 +0900

    Performance improvement

    Use entrySet instead of keySet followed by a lookup per key as the
    former is more efficient.

    Issue: SPR-12363