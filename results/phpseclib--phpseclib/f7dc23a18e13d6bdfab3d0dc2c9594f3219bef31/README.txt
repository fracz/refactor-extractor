commit f7dc23a18e13d6bdfab3d0dc2c9594f3219bef31
Author: Hans-Jürgen Petrich <petrich@tronic-media.com>
Date:   Mon Jan 14 21:23:20 2013 +0700

    3DES: Fixed ContinuousBuffer() in CTR/CFB/OFB

    FIXED: multiple calls to enable/enableContinuousBuffer() work's now in CTR/CFB/OFB
    FIXED: "Illegal string offset" and strlen(array()) Warning/Notice
    Performance improvement in CFB Mode with enableContinuousBuffer() using MODE_MCRYPT