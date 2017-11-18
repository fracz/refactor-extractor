commit 998c413d73fccf92ec19b971a105d6c0f02a4b5c
Author: niloc <niloc@google.com>
Date:   Thu Jul 30 15:39:41 2015 -0700

    Automated g4 rollback of changelist 99517599.

    *** Reason for rollback ***

    VisibleForTesting annotation causing build failure

    *** Original change description ***

    Added use of byte arrays and int arrays to glide gif decoding to improve performance on <L devices. Created general ArrayPool that allows pooling of any array type with a corresponding adapter interface. Follow up CL will replace ByteArrayPool with ArrayPool<byte[]> in Glide.

    ***
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=99520837