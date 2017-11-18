commit 761619286a69e3e56ddceafef79802ffff521abb
Author: Jesse Wilson <jwilson@squareup.com>
Date:   Wed Sep 19 17:18:48 2012 -0400

    Merge: Performance improvements: DiskLruCache, HttpResponseCache.

    Original AOSP/libcore commit from Vladimir Marko:
    Add StrictLineReader for efficient reading of lines
    consistent with Streams.readAsciiLine(). Use this to improve
    DiskLruCache.readJournal() and initialization of
    HttpResponseCache$Entry from InputStream.

    (cherry-pick of e03b551079aae1204e505f1dc24f2b986ef82ec0.)

    Bug: 6739304
    Change-Id: If3083031f1368a9bbbd405c91553d7a205fd4e39