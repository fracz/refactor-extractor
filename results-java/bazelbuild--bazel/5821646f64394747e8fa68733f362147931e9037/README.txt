commit 5821646f64394747e8fa68733f362147931e9037
Author: Googler <noreply@google.com>
Date:   Fri May 8 14:26:14 2015 +0000

    Rewrite of ZipCombiner to improve performance and maintainability

    Poorly performing features of the API have been deprecated in favor of better alternatives:
     - use addZip(File) over addZip(InputStream) or addZip(String, InputStream)
     - use addFile(ZipFileEntry) over addFile(String, Date, InputStream, DirectoryEntryInfo)

    New zip package for high performance ZIP file manipulation. Can directly work with compressed ZIP entry data and has support for Zip64 (forces Zip32 by default).

    --
    MOS_MIGRATED_REVID=93128639