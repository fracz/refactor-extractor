commit c0b6c530766eb5f59e321a1fdbf3d27c05560024
Author: Andreas Huber <andih@google.com>
Date:   Mon Aug 15 09:25:02 2016 -0700

    Initial commit of Java support for hardware binder.

        Squashed commit of the following:

        commit a536e55110a62871662d2f0b1771d7b66bb3cb27
        Author: Andreas Huber <andih@google.com>
        Date:   Mon Aug 15 09:04:45 2016 -0700

        start HwBinder thread pool upon service registration

        Change-Id: I8e91d20e29a5647a36aad083c23d1b594fdc68c5

        commit 0a1882999e0021b68ccebb0306905f17962b8a70
        Author: Andreas Huber <andih@google.com>
        Date:   Thu Aug 11 15:41:05 2016 -0700

        IHwInterface, queryLocalInterface.

        also moved looking up a service into HwBinder. HwRemoteBinder is now an
        implementation detail.

        Change-Id: Ic0b818f5b36c42c608e570fe41a2c0272a6e48df

        commit d99b0b065bde6e0f071245719f8ab31ad25d91be
        Author: Andreas Huber <andih@google.com>
        Date:   Mon Aug 8 09:07:51 2016 -0700

        work in progress

        Change-Id: I4f5612944e9ac1c762ad1fa2e238836e37b484da

        commit 64f027e55780284b981f88a1605c00a0a0f091f4
        Author: Andreas Huber <andih@google.com>
        Date:   Thu Aug 4 10:10:52 2016 -0700

        Squashed commit of the following:

        commit d023d5570ef810094a3d4382158f32faff46c480
        Author: Andreas Huber <andih@google.com>
        Date:   Thu Aug 4 10:09:41 2016 -0700

        Some more cleanup, file organization more closely matches existing code in

        frameworks/base/core/jni/..., don't produce a separate shared library, this is
        now linked as part of libandroid_runtime.so

        Change-Id: I91024f137ff0faae26e72aa9a7163b80b97f0d05

        commit 7cf5370748a9a99be68fac0f32c6793f5bfc4a15
        Author: Andreas Huber <andih@google.com>
        Date:   Thu Aug 4 09:37:44 2016 -0700

        Some more cleanup.

        Change-Id: Ibb89280f51234f8a3dd29ca6d57d6eaaf80a03ea

        commit d70638398ed47c29401d6118e03d640245ecc9b2
        Author: Andreas Huber <andih@google.com>
        Date:   Thu Aug 4 09:31:49 2016 -0700

        hidl::{Parcel,Binder & friends} => hardware::\1

        Change-Id: I8f37a260d4a9afe66e02f7266c54af093502869f

        commit 0fc680aea7753d8de91b4a3806b892dcb4fface8
        Author: Andreas Huber <andih@google.com>
        Date:   Mon Aug 1 14:55:17 2016 -0700

        Squashed commit of the following:

        commit daa714921e20bc209bd7c05aea458569ce539993
        Author: Andreas Huber <andih@google.com>
        Date:   Mon Aug 1 12:09:27 2016 -0700

        Switch to NativeAllocationRegistry for pairing java- with native objects.

        Change-Id: I011fda51e8c24d9e03d6a0ddbcdac9196dc8ab72

        commit 3a02606dba870f8313e1db919720647f686273b4
        Author: Andreas Huber <andih@google.com>
        Date:   Thu Jul 28 11:44:26 2016 -0700

        Squashed commit of the following:

        commit e77b8ff331ed1b5554b4c7c1cd99ed99dbed5f45
        Author: Andreas Huber <andih@google.com>
        Date:   Thu Jul 28 11:15:12 2016 -0700

        no more LOCAL_C_INCLUDES, read/writeEmbeddedBuffer APIs changed to take

        a size_t instead of a uint64_t for both handles and offsets.
        Use android-base/logging instead of utils/Log.h for logs/assertions.

        Change-Id: I1129c75da46e4335efbf94a3394f9bde7d0ad0c6

        commit fbc3a7be0a8cde86b334a677a0e127123beb0051
        Author: Andreas Huber <andih@google.com>
        Date:   Wed Jul 27 16:20:24 2016 -0700

        Squashed commit of the following:

        commit bda9396472eaa32c7643927017ccfdcba8a6aeb3
        Author: Andreas Huber <andih@google.com>
        Date:   Wed Jul 27 16:19:57 2016 -0700

        Code review in progress.

        Change-Id: I66cc3a4ec8caf6feac0d526130c0c3902fd8e2cc

        commit 2887d9589562c3e9834e5d6bb513ab5eff9cdb5f
        Author: Andreas Huber <andih@google.com>
        Date:   Mon Jul 25 12:00:38 2016 -0700

        Squashed commit of the following:

        commit 8a565b0282577508db00a9f6146e75d962b854f4
        Author: Andreas Huber <andih@google.com>
        Date:   Mon Jul 25 12:00:13 2016 -0700

        @hide everything

        Change-Id: I54411d1b0d9cd2ddb66acc1444c23b17b98e58a6

        commit e55a1dd871a2c653bca300335ba9dcc78b3c5ea5
        Author: Andreas Huber <andih@google.com>
        Date:   Tue Jul 19 11:19:07 2016 -0700

        Squashed commit of the following:

        commit c9ae58af694ae2c8451486fe83fab0f6e1398047
        Author: Andreas Huber <andih@google.com>
        Date:   Tue Jul 19 10:10:56 2016 -0700

        switch to a better way of finishing/sending stub transactions

        Change-Id: I7e237f57f4dbd69d05caeb62de0876c40feaa1cf

        commit 0af00b5c41bf808f31300a565467d463d8df0636
        Author: Andreas Huber <andih@google.com>
        Date:   Mon Jul 18 15:32:09 2016 -0700

        exceptions >> assertions

        Change-Id: I098d6e170525539b81aae5e8890c8460ec78df18

        commit f5e1363c2e6b7713ae1460cd8dc22872deebedc4
        Author: Andreas Huber <andih@google.com>
        Date:   Mon Jul 18 11:57:04 2016 -0700

        Squashed commit of the following:

        commit b8716cdf75ac1785f6decb6d236d689c087889cd
        Author: Andreas Huber <andih@google.com>
        Date:   Mon Jul 18 11:56:35 2016 -0700

        Support for {read,write}StringVector.

        Change-Id: I816e4fc22afde960a481c0fb4c3d8531bffa6e56

        commit 34279699169c9ba5fdc03055a863432a2edd1901
        Author: Andreas Huber <andih@google.com>
        Date:   Mon Jul 18 10:08:32 2016 -0700

        LEGO -> Hw

        Change-Id: Ib63ef44cfd13e5a2c3a358d055aad162163d8600

        commit eb2b09c4a03ba785363a8b230a89718ef319c35b
        Author: Andreas Huber <andih@google.com>
        Date:   Fri Jul 15 14:01:11 2016 -0700

        Squashed commit of the following:

        commit c657839a54cf6054e1983a59a59bfcc0854cc952
        Author: Andreas Huber <andih@google.com>
        Date:   Fri Jul 15 12:55:04 2016 -0700

        Support for {read,write}StringArray and stubs for {read,write}StringVector

        Also refactored existing code to take advantage of HidlSupport.h

        Change-Id: I55613d0c66706008d1f96576b9f7a500d52b2cd3

        commit 5d29cd53c4f2a9fe0cdc9052e4041c92d0873042
        Author: Andreas Huber <andih@google.com>
        Date:   Thu Jul 14 16:38:09 2016 -0700

        Squashed commit of the following:

        commit 0a7e49f20d18a7eebda889594cc1bc5511ce2fdc
        Author: Andreas Huber <andih@google.com>
        Date:   Tue Jul 12 14:21:56 2016 -0700

        support all primitive array and vector read/write operations on LEGOParcel

        Change-Id: I200d1dccedc0fd752c297714e8e0df967fdb653b

        commit e0401c2fabc548681d7fd304c09b4ca8838bb0b5
        Author: Andreas Huber <andih@google.com>
        Date:   Tue Jul 12 09:53:42 2016 -0700

        Added LEGOParcel.{read,write}{Int8,Int16}

        Change-Id: I0d9cc149802837593db606692694dbce1b12e521

        commit 9187f7a349c0861549df2c534803e03066d22b4a
        Author: Andreas Huber <andih@google.com>
        Date:   Mon Jul 11 16:10:50 2016 -0700

        Better handling of temporary storage in JLEGOParcel

        Change-Id: I31cdf7b6f1f4bd486661288346cf530c9e92beaa

        commit dc2e5f6f0bb737c739e1dab356efeac945c000c3
        Author: Andreas Huber <andih@google.com>
        Date:   Mon Jul 11 14:18:09 2016 -0700

        proper (read/writeBuffer based) marshalling of strings and int32 arrays

        Change-Id: I2fadd2f831cc0acb50a41222525e71f29b57e79e

        commit 6463a03074fa514ea8b7cb61e475c246e6136b43
        Author: Andreas Huber <andih@google.com>
        Date:   Mon Jul 11 13:13:31 2016 -0700

        support for ILEGOBinder marshalling

        Change-Id: I8ceea45dfcb8460e59a05999ccdcad319650dede

        commit 8c26a006ded339043677d3607884da0abb3e742d
        Author: Andreas Huber <andih@google.com>
        Date:   Fri Jul 8 16:42:38 2016 -0700

        A quick and dirty fix for the

        "storage must stay around until transact() returns" issue.

        Change-Id: I75ac09391cdc23dcc6e154b32eff945f29d00cc0

        commit df7d2c1e4e33d6554ab16e33c90bdf30ce2a136d
        Author: Andreas Huber <andih@google.com>
        Date:   Fri Jul 8 08:48:31 2016 -0700

        Squashed commit of the following:

        commit c42764acbaefab5ffc829dd1a3b60aa252643ae6
        Author: Andreas Huber <andih@google.com>
        Date:   Thu Jul 7 16:51:10 2016 -0700

        An attempt at LEGOParcel.{read,write}Int32Vector

        Change-Id: I9dc54828ad10c8cec8ca292db011925cd60893e2

        commit faa502ac98d28f3eff10a4c16443059ca316c76b
        Author: Andreas Huber <andih@google.com>
        Date:   Thu Jul 7 14:58:24 2016 -0700

        Support for LEGOParcel.{read,write}{String,Int32Array}

        Change-Id: I38317f22ff977bed2f8203c24333bbb7de6f1cc5

        commit ba048b13eb014472130094fafd0fe982363d7add
        Author: Andreas Huber <andih@google.com>
        Date:   Wed Jul 6 16:41:14 2016 -0700

        LEGO{Binder,Parcel,RemoteBinder} java classes

    Change-Id: If1098ab921a11bae8eca2a70a3c3070e4daa0ea2