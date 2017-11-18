commit 7cfd024caaf78080a5d0d7602767f780f6fe7017
Author: jwilson <jwilson@squareup.com>
Date:   Sat Apr 16 01:32:59 2016 -0400

    Implement BufferedSource.select(List<ByteString>).

    I spent a lot of time working on an alternate version that took a
    Selection object. It modeled a 'compiled' variant of the List<String>
    that had certain optimizations applied. Unfortunately the benefits
    of these optimizations weren't worth the complexity: they improved
    performance only when the choices were very long strings.

    This API is not particularly extensible. If we ever found optimizations
    that require the bytestrings to be sorted, or converted into a trie,
    or something else, this API prevents that. From my investigation this
    is a reasonable tradeoff; those approaches have been explored and aren't
    that compelling!