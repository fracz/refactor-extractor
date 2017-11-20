commit 73e2ad96df99f4bed832d85901d2a210ac22c39e
Author: rvesse <rvesse@dotnetrdf.org>
Date:   Mon Feb 2 15:13:53 2015 -0800

    Continued test profile refactoring (TACHYON-221)

    The localTest profile in the core module is no longer directly relevant
    since it will always be used regardless as the core module now only
    provides the local under file system implementation.

    The glusterfsTest profile is moved to the underfs-glusterfs module.
    Unlike the HDFS tests this remains a profile since Gluster FS requires a
    proper cluster setup and can't be simulated.