commit 6661788748daccfc8d08b4a17a66beb4f01fc7b8
Author: Phillip Webb <pwebb@vmware.com>
Date:   Mon Feb 11 18:42:21 2013 -0800

    Ensure HTTP classes don't close streams

    Prior to this commit several HTTP classes made use of FileCopyUtils
    when reading from or writing to streams. This has the unfortunate
    side effect of closing streams that should really be left open.

    The problem is particularly noticeable when dealing with a
    FormHttpMessageConverter that is writing a multi-part response.

    Relevant HTTP classes have now been refactored to make use of a new
    StreamUtils class that works in a similar way FileCopyUtils but does
    not close streams.

    The NonClosingOutputStream class from SimpleStreamingClientHttpRequest
    has also been refactored to a StreamUtils method.

    Issue: SPR-10095