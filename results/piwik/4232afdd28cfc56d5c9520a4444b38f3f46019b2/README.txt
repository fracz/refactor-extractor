commit 4232afdd28cfc56d5c9520a4444b38f3f46019b2
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Wed Mar 30 22:29:25 2011 +0000

    Fixes #2243
     * Enabling the bulk loading on BLOB records.
     * Had to first run the blobs through bin2hex... indeed even though it *looked* like it was working at first, the tests once again saved us from a *VERY* nasty bug which would have shown up only later and be extremely difficult to find. LOAD DATA INFILE was working fine most of the time, but a few tests were failing! Then I discovered that the file containing the blob wasn't loaded properly in all cases, unless the binary blob was hex'ed.
     I ran quick performance test and it seems approximately the same performance, and uses the same disk space as well.
     However in some cases, it will definitely improve performance.
     memory usage somehow is much less in my test! so definitely a great point.
     it would have been nice to have versionning in the archive_blob table, since now we have 2 different data types (old blob are pure binary, new blobs are hex'ed) but it is working nonetheless (Old blobs will fail the first gzuncompress(hex2bin()) then fallback to gzuncompress() only


    git-svn-id: http://dev.piwik.org/svn/trunk@4253 59fd770c-687e-43c8-a1e3-f5a4ff64c105