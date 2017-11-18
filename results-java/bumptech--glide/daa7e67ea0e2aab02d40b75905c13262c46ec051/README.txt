commit daa7e67ea0e2aab02d40b75905c13262c46ec051
Author: niloc <niloc@google.com>
Date:   Thu Jul 30 15:06:22 2015 -0700

    Added use of byte arrays and int arrays to glide gif decoding to improve performance on <L devices. Created general ArrayPool that allows pooling of any array type with a corresponding adapter interface. Follow up CL will replace ByteArrayPool with ArrayPool<byte[]> in Glide.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=99517599