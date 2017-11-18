commit dc9d933d7419a0513f1bc84bd3faae47391d7a48
Author: Idel Pivnitskiy <Idel.Pivnitskiy@gmail.com>
Date:   Sat Jul 19 15:55:15 2014 +0400

    Fixes for compression codecs

    Motivation:

    Fixed founded mistakes in compression codecs.

    Modifications:

    - Changed return type of ZlibUtil.inflaterException() from CompressionException to DecompressionException
    - Updated @throws in javadoc of JZlibDecoder to throw DecompressionException instead of CompressionException
    - Fixed JdkZlibDecoder to throw DecompressionException instead of CompressionException
    - Removed unnecessary empty lines in JdkZlibEncoder and JZlibEncoder
    - Removed public modifier from Snappy class
    - Added MAX_UNCOMPRESSED_DATA_SIZE constant in SnappyFramedDecoder
    - Used in.readableBytes() instead of (in.writerIndex() - in.readerIndex()) in SnappyFramedDecoder
    - Added private modifier for enum ChunkType in SnappyFramedDecoder
    - Fixed potential bug (sum overflow) in Bzip2HuffmanAllocator.first(). For more info, see http://googleresearch.blogspot.ru/2006/06/extra-extra-read-all-about-it-nearly.html

    Result:

    Fixed sum overflow in Bzip2HuffmanAllocator, improved exceptions in ZlibDecoder implementations, hid Snappy class