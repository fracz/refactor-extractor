commit c36f2b226dd37050176fcd79b2abf376a9eb0d0e
Author: Jake Wharton <jw@squareup.com>
Date:   Wed Sep 19 14:43:45 2012 -0700

    Use StrictLineReader for journal.

    Original AOSP/libcore commit from Vladimir Marko:
    Add StrictLineReader for efficient reading of lines
    consistent with Streams.readAsciiLine(). Use this to improve
    DiskLruCache.readJournal() and initialization of
    HttpResponseCache from InputStream.

    Bug: 6739304
    Change-Id: If3083031f1368a9bbbd405c91553d7a205fd4e39