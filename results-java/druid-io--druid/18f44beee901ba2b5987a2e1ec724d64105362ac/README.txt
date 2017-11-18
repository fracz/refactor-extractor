commit 18f44beee901ba2b5987a2e1ec724d64105362ac
Author: Charles Allen <charles@allen-net.com>
Date:   Wed Nov 19 11:10:59 2014 -0800

    CompressedObjectStrategy improvements

    * Added more unit tests
    * Now properly uses safe / fast decompressor for LZ4
    * Now chooses fastest lz4 instance instead of only looking at Java implmentations
    * Encapsulate ResourceHolder in try-with-resources to make sure they close correctly