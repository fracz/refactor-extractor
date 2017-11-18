commit 226f52575da22ef08ad4e554ba14d34384f8eb73
Author: Martin Thompson <mjpt777@gmail.com>
Date:   Thu Aug 11 07:17:25 2011 +0000

    Removed the indirection layers for sequence tracking which should give a performance improvement to real world applications.  All sequences are now tracked without using any class that have the potential to be mega morphic.