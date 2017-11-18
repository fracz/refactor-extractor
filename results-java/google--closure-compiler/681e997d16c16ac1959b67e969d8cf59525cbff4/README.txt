commit 681e997d16c16ac1959b67e969d8cf59525cbff4
Author: dimvar <dimvar@google.com>
Date:   Thu Oct 12 22:11:52 2017 -0700

    [NTI] Move simple inference into its own file.

    This improves clarity and also allows us to use simple inference outside ProcessScope.
    In a follow-up CL, I will use this to annotate more externs nodes.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=172056865