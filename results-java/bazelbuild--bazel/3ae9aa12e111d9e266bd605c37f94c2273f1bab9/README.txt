commit 3ae9aa12e111d9e266bd605c37f94c2273f1bab9
Author: Googler <noreply@google.com>
Date:   Fri Apr 3 21:13:50 2015 +0000

    Automated [] rollback of [].

    *** Reason for rollback ***

    New ZipCombiner creates malformed output ZIP files when input ZIP files contain more than 65535 entries, the maximum amount for non-64-bit ZIP files.

    *** Original change description ***

    Rewrite of ZipCombiner to improve performance and maintainability. Added devtools/build/zip to allow reading and writing of ZIP files without requiring decompressing file data to manipulate them.

    ZipCombiner API has some changes. ZipCombiner#addZip takes a File instead of InputStream. ZipCombiner#addFile takes a ZipFileEntry instead of DirectoryEntryInfo

    --
    MOS_MIGRATED_REVID=90279976